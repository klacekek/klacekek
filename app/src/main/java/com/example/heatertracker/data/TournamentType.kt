package com.example.heatertracker.data

enum class TournamentType(val displayName: String) {
    MTT("Multi-table Tournament"),
    SIT_AND_GO("Sit & Go"),
    BOUNTY("Bounty"),
    SATELLITE("Satellite"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(name: String): TournamentType =
            values().firstOrNull { it.displayName == name } ?: OTHER
    }
}
