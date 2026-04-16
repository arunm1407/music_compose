package com.example.myapplication.data

import com.example.myapplication.network.MusicApi
import com.example.myapplication.network.SaavnTrack

enum class SongSource { LOCAL, STREAM }

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String,
    val mediaUrl: String,
    val durationMs: Long = 0L,
    val source: SongSource = SongSource.STREAM,
    val filePath: String? = null,
    val albumId: Long? = null,
    val artistId: Long? = null,
    val genreId: Long? = null,
    val genre: String = "",
    val year: Int = 0,
    val trackNumber: Int = 0,
    val dateAdded: Long = 0L,
)

fun SaavnTrack.toSong(): Song {
    val decryptedUrl = MusicApi.decryptMediaUrl(moreInfo.encryptedMediaUrl)
    val mediaUrl = if (decryptedUrl.isNotBlank()) {
        decryptedUrl
            .replace("_96.mp4", "_160.mp4")
            .replace("_96.m4a", "_160.m4a")
    } else ""

    return Song(
        id = id,
        title = title,
        artist = artistName,
        album = moreInfo.album,
        coverUrl = highResImage,
        mediaUrl = mediaUrl,
        durationMs = (moreInfo.duration.toLongOrNull() ?: 0L) * 1000L,
        source = SongSource.STREAM,
    )
}
