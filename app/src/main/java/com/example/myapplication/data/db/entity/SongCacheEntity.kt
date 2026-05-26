package com.example.myapplication.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_cache")
data class SongCacheEntity(
    @PrimaryKey val songId: String,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val coverUrl: String = "",
    val mediaUrl: String = "",
    val durationMs: Long = 0L,
    val source: String = "STREAM",
    val filePath: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)
