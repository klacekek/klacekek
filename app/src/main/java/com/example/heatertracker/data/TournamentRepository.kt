package com.example.heatertracker.data

import kotlinx.coroutines.flow.Flow

class TournamentRepository(private val dao: TournamentDao) {

    fun observeEntries(): Flow<List<TournamentEntry>> = dao.observeTournaments()

    suspend fun upsert(entry: TournamentEntry) = dao.upsert(entry)

    suspend fun delete(entry: TournamentEntry) = dao.delete(entry)
}
