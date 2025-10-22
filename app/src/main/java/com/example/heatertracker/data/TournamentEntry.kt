package com.example.heatertracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "tournaments")
data class TournamentEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val buyIn: Double,
    val payout: Double,
    val type: TournamentType,
    val notes: String = ""
) {
    val profit: Double
        get() = payout - buyIn

    val isInTheMoney: Boolean
        get() = payout > 0.0
}
