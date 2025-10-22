package com.example.heatertracker.data

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromTournamentType(value: String?): TournamentType? = value?.let { TournamentType.valueOf(it) }

    @TypeConverter
    fun toTournamentType(type: TournamentType?): String? = type?.name
}
