package com.example.heatertracker.app

import android.app.Application
import com.example.heatertracker.data.HeaterDatabase
import com.example.heatertracker.data.TournamentEntry
import com.example.heatertracker.data.TournamentRepository
import com.example.heatertracker.data.TournamentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.SupervisorJob

class HeaterTrackerApplication : Application() {
    lateinit var repository: TournamentRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val database = HeaterDatabase.getDatabase(this)
        repository = TournamentRepository(database.tournamentDao())
        seedSampleData()
    }

    private fun seedSampleData() {
        applicationScope.launch {
            val existing = repository.observeEntries().firstOrNull()
            if (!existing.isNullOrEmpty()) return@launch

            val sampleEntries = listOf(
                TournamentEntry(
                    date = LocalDate.now().minusDays(5),
                    buyIn = 55.0,
                    payout = 0.0,
                    type = TournamentType.MTT,
                    notes = "Deep run, busted before the money"
                ),
                TournamentEntry(
                    date = LocalDate.now().minusDays(3),
                    buyIn = 33.0,
                    payout = 120.0,
                    type = TournamentType.BOUNTY,
                    notes = "Captured two bounties"
                ),
                TournamentEntry(
                    date = LocalDate.now().minusDays(1),
                    buyIn = 22.0,
                    payout = 0.0,
                    type = TournamentType.SIT_AND_GO,
                    notes = "Lost flip on bubble"
                )
            )
            sampleEntries.forEach { repository.upsert(it) }
        }
    }
}
