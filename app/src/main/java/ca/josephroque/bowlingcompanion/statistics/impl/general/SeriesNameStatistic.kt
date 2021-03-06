package ca.josephroque.bowlingcompanion.statistics.impl.general

import android.os.Parcel
import android.os.Parcelable
import ca.josephroque.bowlingcompanion.R
import ca.josephroque.bowlingcompanion.common.interfaces.parcelableCreator
import ca.josephroque.bowlingcompanion.statistics.StatisticsCategory
import ca.josephroque.bowlingcompanion.statistics.StringStatistic
import ca.josephroque.bowlingcompanion.statistics.unit.GameUnit
import ca.josephroque.bowlingcompanion.statistics.unit.SeriesUnit
import ca.josephroque.bowlingcompanion.statistics.unit.StatisticsUnit

/**
 * Copyright (C) 2018 Joseph Roque
 *
 * Name of the series whose statistics are on display.
 */
class SeriesNameStatistic(override var value: String = "") : StringStatistic {

    // MARK: Modifiers

    /** @Override */
    override fun modify(unit: StatisticsUnit) {
        when (unit) {
            is GameUnit -> value = unit.prettySeriesDate
            is SeriesUnit -> value = unit.name
        }
    }

    // MARK: Overrides

    override val titleId = Id
    override val id = Id.toLong()
    override val category = StatisticsCategory.General
    override fun isModifiedBy(unit: StatisticsUnit) = true

    // MARK: Parcelable

    companion object {
        /** Creator, required by [Parcelable]. */
        @Suppress("unused")
        @JvmField val CREATOR = parcelableCreator(::SeriesNameStatistic)

        /** Unique ID for the statistic. */
        const val Id = R.string.statistic_series
    }

    /**
     * Construct this statistic from a [Parcel].
     */
    private constructor(p: Parcel): this(value = p.readString()!!)
}
