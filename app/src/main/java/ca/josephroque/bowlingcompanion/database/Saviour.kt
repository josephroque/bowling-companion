package ca.josephroque.bowlingcompanion.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import ca.josephroque.bowlingcompanion.App
import ca.josephroque.bowlingcompanion.common.Android
import ca.josephroque.bowlingcompanion.database.Contract.FrameEntry
import ca.josephroque.bowlingcompanion.database.Contract.GameEntry
import ca.josephroque.bowlingcompanion.database.Contract.MatchPlayEntry
import ca.josephroque.bowlingcompanion.games.Frame
import ca.josephroque.bowlingcompanion.games.Game
import ca.josephroque.bowlingcompanion.games.lane.toInt
import ca.josephroque.bowlingcompanion.matchplay.MatchPlay
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.lang.ref.WeakReference
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Copyright (C) 2018 Joseph Roque
 *
 * Managing saving to the database synchronously and waiting for tasks to finish saving before
 * loading happens.
 */
class Saviour private constructor() {

    companion object {
        @Suppress("unused")
        private const val TAG = "Saviour"

        val instance: Saviour by lazy { Holder.INSTANCE }
    }

    private object Holder { val INSTANCE = Saviour() }

    private var saviourIsRunning = AtomicBoolean(false)

    private val saveQueue: Queue<Job> = LinkedList()

    init {
        launchSaviourProcessor()
    }

    // MARK: Saviour

    fun wait(): Deferred<Unit> {
        return async(CommonPool) {
            launchSaviourProcessor()
            while (saveQueue.isNotEmpty()) {
                delay(50)
            }
        }
    }

    fun saveFrame(weakContext: WeakReference<Context>, score: Int, frame: Frame) {
        val job = launch(context = Android, start = CoroutineStart.LAZY) {
            val strongContext = weakContext.get() ?: return@launch
            val database = DatabaseHelper.getInstance(strongContext).writableDatabase
            database.beginTransaction()
            try {
                // Save the score
                val values = ContentValues()
                values.put(GameEntry.COLUMN_SCORE, score)
                database.update(GameEntry.TABLE_NAME,
                        values,
                        "${GameEntry._ID}=?",
                        arrayOf(frame.gameId.toString()))

                // Save the frame
                writeFrameToDatabase(database, frame)

                database.setTransactionSuccessful()
            } catch (ex: Exception) {
                Log.e(TAG, "Fatal error. Game could not save.")
                throw ex
            } finally {
                database.endTransaction()
            }
        }

        saveQueue.offer(job)
        launchSaviourProcessor()
    }

    fun saveMatchPlay(weakContext: WeakReference<Context>, matchPlay: MatchPlay) {
        val job = launch(context = Android, start = CoroutineStart.LAZY) {
            val strongContext = weakContext.get() ?: return@launch
            val database = DatabaseHelper.getInstance(strongContext).writableDatabase
            database.beginTransaction()
            try {
                writeMatchPlayToDatabase(database, matchPlay)
                database.setTransactionSuccessful()
            } catch (ex: Exception) {
                Log.e(TAG, "Fatal error. Game could not save.")
                throw ex
            } finally {
                database.endTransaction()
            }
        }

        saveQueue.offer(job)
        launchSaviourProcessor()
    }

    fun saveGame(weakContext: WeakReference<Context>, game: Game) {
        val job = launch(context = Android, start = CoroutineStart.LAZY) {
            val strongContext = weakContext.get() ?: return@launch
            val database = DatabaseHelper.getInstance(strongContext).writableDatabase
            database.beginTransaction()
            try {
                val values = ContentValues().apply {
                    put(GameEntry.COLUMN_SCORE, game.score)
                    put(GameEntry.COLUMN_IS_LOCKED, game.isLocked)
                    put(GameEntry.COLUMN_IS_MANUAL, game.isManual)
                }
                database.update(GameEntry.TABLE_NAME,
                        values,
                        "${GameEntry._ID}=?",
                        arrayOf(game.id.toString()))

                for (frame in game.frames) {
                    writeFrameToDatabase(database, frame)
                }

                writeMatchPlayToDatabase(database, game.matchPlay)

                database.setTransactionSuccessful()
            } catch (ex: Exception) {
                Log.e(TAG, "Fatal error. Game could not save.")
                throw ex
            } finally {
                database.endTransaction()
            }
        }

        saveQueue.offer(job)
        launchSaviourProcessor()
    }

    // MARK: Private functions

    private fun launchSaviourProcessor() {
        if (saviourIsRunning.get()) {
            return
        }

        saviourIsRunning.set(true)
        launch(CommonPool) {
            while (App.isRunning.get() || saveQueue.isNotEmpty()) {
                val saveRoutine = saveQueue.poll()
                if (!saveRoutine.isCompleted && !saveRoutine.isCancelled) {
                    saveRoutine.join()
                } else {
                    Log.e(TAG, "Saviour thread already processed - $saveRoutine")
                }
            }

            saviourIsRunning.set(false)
        }
    }

    private fun writeFrameToDatabase(database: SQLiteDatabase, frame: Frame) {
        val values = ContentValues().apply {
            for (i in 0 until Frame.NUMBER_OF_BALLS) {
                put(FrameEntry.COLUMN_PIN_STATE[i], frame.pinState[i].toInt())
            }
            put(FrameEntry.COLUMN_IS_ACCESSED, if (frame.isAccessed) 1 else 0)
            put(FrameEntry.COLUMN_FOULS, frame.dbFouls)
        }
        database.update(FrameEntry.TABLE_NAME,
                values,
                "${FrameEntry._ID}=?",
                arrayOf(frame.id.toString()))
    }

    private fun writeMatchPlayToDatabase(database: SQLiteDatabase, matchPlay: MatchPlay) {
        // Save the match play result to the game
        var values = ContentValues()
        values.put(GameEntry.COLUMN_MATCH_PLAY, matchPlay.result.ordinal)
        database.update(GameEntry.TABLE_NAME,
                values,
                "${GameEntry._ID}=?",
                arrayOf(matchPlay.gameId.toString()))

        // Save the match play details
        values = ContentValues().apply {
            put(MatchPlayEntry.COLUMN_GAME_ID, matchPlay.gameId)
            put(MatchPlayEntry.COLUMN_OPPONENT_NAME, matchPlay.opponentName)
            put(MatchPlayEntry.COLUMN_OPPONENT_SCORE, matchPlay.opponentScore)
        }

        /*
         * Due to the way this method was originally implemented, when match play results were updated,
         * often the wrong row in the table was altered. This bug prevented users from saving match play
         * results under certain circumstances. This has been fixed, but the old data cannot be safely
         * removed all at once, without potentially deleting some of the user's real data. As a fix, when
         * a user now saves match play results, any old results for *only that game* are deleted, and the
         * new results are inserted, as seen below.
         */
        database.delete(MatchPlayEntry.TABLE_NAME,
                "${MatchPlayEntry.COLUMN_GAME_ID}=?",
                arrayOf(matchPlay.gameId.toString()))

        database.insert(MatchPlayEntry.TABLE_NAME,
                null,
                values)
    }
}
