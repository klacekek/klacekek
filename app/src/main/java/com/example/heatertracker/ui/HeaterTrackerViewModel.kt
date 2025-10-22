package com.example.heatertracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.heatertracker.app.HeaterTrackerApplication
import com.example.heatertracker.data.TournamentEntry
import com.example.heatertracker.data.TournamentRepository
import com.example.heatertracker.data.TournamentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class HeaterTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TournamentRepository =
        (application as HeaterTrackerApplication).repository

    private val _uiState = MutableStateFlow(HeaterTrackerUiState())
    val uiState: StateFlow<HeaterTrackerUiState> = _uiState.asStateFlow()

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

    init {
        viewModelScope.launch {
            repository.observeEntries().collect { entries ->
                _uiState.update { state ->
                    state.copy(
                        entries = entries.map { it.toUiModel(formatter) },
                        totalProfit = entries.sumOf { it.profit },
                        totalBuyIns = entries.sumOf { it.buyIn },
                        itmPercentage = calculateItm(entries),
                        cumulativePoints = buildCumulativeProfit(entries)
                    )
                }
            }
        }
    }

    fun addTournament(
        date: LocalDate,
        buyIn: Double,
        payout: Double,
        type: TournamentType,
        notes: String
    ) {
        viewModelScope.launch {
            repository.upsert(
                TournamentEntry(
                    date = date,
                    buyIn = buyIn,
                    payout = payout,
                    type = type,
                    notes = notes
                )
            )
        }
    }

    private fun calculateItm(entries: List<TournamentEntry>): Double {
        if (entries.isEmpty()) return 0.0
        val itmCount = entries.count { it.payout > 0.0 }
        return itmCount.toDouble() / entries.size * 100.0
    }

    private fun buildCumulativeProfit(entries: List<TournamentEntry>): List<ProfitPoint> {
        if (entries.isEmpty()) return emptyList()
        val sorted = entries.sortedBy { it.date }
        var running = 0.0
        return sorted.map { entry ->
            running += entry.profit
            ProfitPoint(date = entry.date, value = running)
        }
    }

    private fun TournamentEntry.toUiModel(formatter: DateTimeFormatter): TournamentEntryUiModel =
        TournamentEntryUiModel(
            id = id,
            date = date,
            dateLabel = formatter.format(date),
            buyIn = buyIn,
            payout = payout,
            type = type,
            notes = notes,
            profit = profit,
            isInTheMoney = isInTheMoney
        )
}

data class HeaterTrackerUiState(
    val entries: List<TournamentEntryUiModel> = emptyList(),
    val totalProfit: Double = 0.0,
    val totalBuyIns: Double = 0.0,
    val itmPercentage: Double = 0.0,
    val cumulativePoints: List<ProfitPoint> = emptyList()
)

data class TournamentEntryUiModel(
    val id: Int,
    val date: LocalDate,
    val dateLabel: String,
    val buyIn: Double,
    val payout: Double,
    val type: TournamentType,
    val notes: String,
    val profit: Double,
    val isInTheMoney: Boolean
)

data class ProfitPoint(
    val date: LocalDate,
    val value: Double
)
