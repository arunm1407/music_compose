package com.example.myapplication.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String,
    val mediaUrl: String,
    val localPath: String,
    val durationMs: Long = 0L,
    val downloadedAt: Long = System.currentTimeMillis(),
)
