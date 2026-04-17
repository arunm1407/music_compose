package com.example.myapplication.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_lyrics")
data class LyricsEntity(
    @PrimaryKey val songId: String,
    val plainLyrics: String = "",
    val syncedLyrics: String = "",
    val fetchedAt: Long = System.currentTimeMillis(),
)
