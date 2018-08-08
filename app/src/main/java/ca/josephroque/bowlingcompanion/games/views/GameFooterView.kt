package ca.josephroque.bowlingcompanion.games.views

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import ca.josephroque.bowlingcompanion.R
import ca.josephroque.bowlingcompanion.matchplay.MatchPlayResult
import kotlinx.android.synthetic.main.view_game_footer.view.divider as divider
import kotlinx.android.synthetic.main.view_game_footer.view.iv_clear_pins as clearPinsIcon
import kotlinx.android.synthetic.main.view_game_footer.view.iv_foul as foulIcon
import kotlinx.android.synthetic.main.view_game_footer.view.iv_lock as lockIcon
import kotlinx.android.synthetic.main.view_game_footer.view.iv_match_play as matchPlayIcon

/**
 * Copyright (C) 2018 Joseph Roque
 *
 * Footer view to display game and frame controls at the foot of the game details.
 */
class GameFooterView : ConstraintLayout, View.OnClickListener {

    companion object {
        /** Logging identifier. */
        @Suppress("unused")
        private const val TAG = "GameFooterView"

        /** Tag to save super state. */
        private const val SUPER_STATE = "${TAG}_super_state"
        /** Tag to save state of current ball. */
        private const val STATE_CURRENT_BALL = "${TAG}_current_ball"
        /** Tag to save state of match play results. */
        private const val STATE_MATCH_PLAY_RESULT = "${TAG}_match_play_result"
        /** Tag to save the current lock state. */
        private const val STATE_LOCK = "${TAG}_lock"
        /** Tag to save the current foul state. */
        private const val STATE_FOUL = "${TAG}_foul"
        /** Tag to save the current manual score state. */
        private const val STATE_MANUAL_SCORE = "${TAG}_manual"
    }

    /** Delegate for interactions. */
    var delegate: GameFooterInteractionDelegate? = null

    /** The drawable for the clear pin button. */
    var clearIcon: Int = R.drawable.ic_clear_pins_strike
        set(value) {
            field = value
            clearPinsIcon.setImageResource(field)
        }

    /** The current match play result, which determines state of the match play button. */
    var matchPlayResult: MatchPlayResult = MatchPlayResult.NONE
        set(value) {
            field = value
            matchPlayIcon.setImageResource(value.getIcon())
        }

    /** Indicates if the current game is locked, which determines state of the game lock button. */
    var isGameLocked: Boolean = false
        set(value) {
            field = value
            lockIcon.setImageResource(if (value) R.drawable.ic_lock else R.drawable.ic_lock_open)
        }

    /**
     * Indicates if the current ball has the foul enabled, which determines state of
     * the foul button.
     */
    var isFoulActive: Boolean = false
        set(value) {
            field = value
            foulIcon.setImageResource(if (value) R.drawable.ic_foul_active else R.drawable.ic_foul_inactive)
        }

    var isManualScoreSet: Boolean = false
        set(value) {
            field = value
            val visible = if (value) View.GONE else View.VISIBLE
            clearPinsIcon.visibility = visible
            foulIcon.visibility = visible
            divider.visibility = visible
        }

    /** Required constructors */
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.view_game_footer, this, true)

        clearPinsIcon.setOnClickListener(this)
        foulIcon.setOnClickListener(this)
        lockIcon.setOnClickListener(this)
        matchPlayIcon.setOnClickListener(this)
    }

    /** @Override */
    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(SUPER_STATE, super.onSaveInstanceState())
            putInt(STATE_CURRENT_BALL, clearIcon)
            putInt(STATE_MATCH_PLAY_RESULT, matchPlayResult.ordinal)
            putBoolean(STATE_LOCK, isGameLocked)
            putBoolean(STATE_FOUL, isFoulActive)
            putBoolean(STATE_MANUAL_SCORE, isManualScoreSet)
        }
    }

    /** @Override */
    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState: Parcelable? = null
        if (state is Bundle) {
            clearIcon = state.getInt(STATE_CURRENT_BALL)
            matchPlayResult = MatchPlayResult.fromInt(state.getInt(STATE_MATCH_PLAY_RESULT))!!
            isGameLocked = state.getBoolean(STATE_LOCK)
            isFoulActive = state.getBoolean(STATE_FOUL)
            isManualScoreSet = state.getBoolean(STATE_MANUAL_SCORE)
            superState = state.getParcelable(SUPER_STATE)
        }

        super.onRestoreInstanceState(superState)
    }

    /** @Override */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        delegate = null
    }

    /** @Override */
    override fun onClick(v: View?) {
        val view = v ?: return
        when (view.id) {
            R.id.iv_clear_pins -> delegate?.onClearPins()
            R.id.iv_foul -> delegate?.onFoulToggle()
            R.id.iv_lock -> delegate?.onLockToggle()
            R.id.iv_match_play -> delegate?.onMatchPlaySettings()
        }
    }
    /**
     * Handle interactions with this view.
     */
    interface GameFooterInteractionDelegate {
        /**
         * Indicate the user wishes to lock or unlock the game.
         */
        fun onLockToggle()

        /**
         * Indicate the user wishes to clear the pins.
         */
        fun onClearPins()

        /**
         * Indicate the user wishes to add or remove a foul.
         */
        fun onFoulToggle()

        /**
         * Indicate the user wishes to adjust match play settings.
         */
        fun onMatchPlaySettings()
    }
}
