package com.example.myapplication.data

import com.example.myapplication.data.db.entity.DownloadEntity
import com.example.myapplication.data.db.entity.FavoriteEntity
import com.example.myapplication.data.db.entity.PlayHistoryEntity
import com.example.myapplication.data.db.entity.SongCacheEntity
import java.io.File

fun Song.playbackUri(): String {
    if (filePath != null && File(filePath).exists()) return File(filePath).toURI().toString()
    return mediaUrl
}

fun Song.isPlayable(): Boolean = playbackUri().isNotBlank()

/** Prefer local file paths and merge stream metadata from the freshest source. */
fun mergeSongForPlayback(cached: Song?, catalog: Song?): Song? {
    when {
        cached == null && catalog == null -> return null
        cached == null -> return catalog
        catalog == null -> return cached
        else -> {
            val cachedLocalPath = cached.filePath?.takeIf { File(it).exists() }
            val catalogLocalPath = catalog.filePath?.takeIf { File(it).exists() }
            return when {
                catalogLocalPath != null -> catalog.copy(filePath = catalogLocalPath)
                cachedLocalPath != null -> cached.copy(filePath = cachedLocalPath)
                catalog.mediaUrl.isNotBlank() -> catalog
                cached.mediaUrl.isNotBlank() -> cached
                else -> catalog.takeIf { it.isPlayable() } ?: cached.takeIf { it.isPlayable() }
            }
        }
    }
}

fun mergeCatalogSongs(vararg sources: List<Song>): List<Song> {
    val merged = linkedMapOf<String, Song>()
    sources.forEach { list ->
        list.forEach { song ->
            val existing = merged[song.id]
            merged[song.id] = when {
                existing == null -> song
                song.filePath != null -> song
                existing.filePath != null -> existing
                else -> song
            }
        }
    }
    return merged.values.toList()
}

fun Song.toCacheEntity() = SongCacheEntity(
    songId = id,
    title = title,
    artist = artist,
    album = album,
    coverUrl = coverUrl,
    mediaUrl = mediaUrl,
    durationMs = durationMs,
    source = source.name,
    filePath = filePath,
)

fun SongCacheEntity.toSong() = Song(
    id = songId,
    title = title,
    artist = artist,
    album = album,
    coverUrl = coverUrl,
    mediaUrl = mediaUrl,
    durationMs = durationMs,
    source = try { SongSource.valueOf(source) } catch (_: Exception) { SongSource.STREAM },
    filePath = filePath,
)

fun DownloadEntity.toSong() = Song(
    id = songId,
    title = title,
    artist = artist,
    album = album,
    coverUrl = coverUrl,
    mediaUrl = mediaUrl,
    durationMs = durationMs,
    source = SongSource.LOCAL,
    filePath = localPath,
)

fun FavoriteEntity.toSong() = Song(
    id = songId,
    title = songTitle,
    artist = songArtist,
    album = songAlbum,
    coverUrl = songCoverUrl,
    mediaUrl = songMediaUrl,
    durationMs = songDurationMs,
    source = try { SongSource.valueOf(songSource) } catch (_: Exception) { SongSource.STREAM },
    filePath = songFilePath,
)

fun PlayHistoryEntity.toSong() = Song(
    id = songId,
    title = songTitle,
    artist = songArtist,
    album = songAlbum,
    coverUrl = songCoverUrl,
    mediaUrl = songMediaUrl,
    durationMs = songDurationMs,
    source = try { SongSource.valueOf(songSource) } catch (_: Exception) { SongSource.STREAM },
    filePath = songFilePath,
)
