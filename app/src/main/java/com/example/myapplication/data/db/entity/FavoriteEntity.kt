package com.example.myapplication.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: String,
    val songTitle: String = "",
    val songArtist: String = "",
    val songAlbum: String = "",
    val songCoverUrl: String = "",
    val songMediaUrl: String = "",
    val songDurationMs: Long = 0L,
    val songSource: String = "STREAM",
    val songFilePath: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
)
