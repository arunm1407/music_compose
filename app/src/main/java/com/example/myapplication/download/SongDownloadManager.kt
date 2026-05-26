package com.example.myapplication.download

import android.content.Context
import com.example.myapplication.data.Song
import com.example.myapplication.data.SongSource
import com.example.myapplication.data.db.dao.DownloadDao
import com.example.myapplication.data.db.dao.SongCacheDao
import com.example.myapplication.data.db.entity.DownloadEntity
import com.example.myapplication.data.db.entity.SongCacheEntity
import com.example.myapplication.network.MusicApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

data class DownloadProgress(
    val songId: String,
    val progress: Float,
    val isDownloading: Boolean,
)

class SongDownloadManager(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val songCacheDao: SongCacheDao,
) {
    private val _progress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val progress: StateFlow<Map<String, DownloadProgress>> = _progress.asStateFlow()

    private val activeDownloads = mutableSetOf<String>()

    private val downloadsDir: File
        get() = File(context.filesDir, "downloads").also { it.mkdirs() }

    suspend fun download(song: Song): Result<Song> = withContext(Dispatchers.IO) {
        if (song.id in activeDownloads) {
            return@withContext Result.failure(IllegalStateException("Download already in progress"))
        }
        if (song.mediaUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No stream URL"))
        }
        val existing = downloadDao.getById(song.id)
        if (existing != null && File(existing.localPath).exists()) {
            return@withContext Result.success(existing.toSong())
        }

        activeDownloads.add(song.id)
        updateProgress(song.id, 0f, true)
        try {
            val file = File(downloadsDir, "${song.id}.mp3")
            val request = Request.Builder().url(song.mediaUrl).build()
            val response = MusicApi.client.newCall(request).execute()
            if (!response.isSuccessful) {
                updateProgress(song.id, 0f, false)
                return@withContext Result.failure(IllegalStateException("Download failed"))
            }
            val body = response.body ?: run {
                updateProgress(song.id, 0f, false)
                return@withContext Result.failure(IllegalStateException("Empty response"))
            }
            val total = body.contentLength()
            body.byteStream().use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    var downloaded = 0L
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (total > 0) updateProgress(song.id, downloaded.toFloat() / total, true)
                    }
                }
            }
            val entity = DownloadEntity(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                coverUrl = song.coverUrl,
                mediaUrl = song.mediaUrl,
                localPath = file.absolutePath,
                durationMs = song.durationMs,
            )
            downloadDao.insert(entity)
            songCacheDao.upsert(entity.toCacheEntity())
            updateProgress(song.id, 1f, false)
            Result.success(entity.toSong())
        } catch (e: Exception) {
            updateProgress(song.id, 0f, false)
            Result.failure(e)
        } finally {
            activeDownloads.remove(song.id)
        }
    }

    suspend fun delete(songId: String) = withContext(Dispatchers.IO) {
        downloadDao.getById(songId)?.let { File(it.localPath).delete() }
        downloadDao.delete(songId)
        _progress.value = _progress.value - songId
    }

    suspend fun getLocalPath(songId: String): String? = withContext(Dispatchers.IO) {
        val entity = downloadDao.getById(songId) ?: return@withContext null
        if (File(entity.localPath).exists()) entity.localPath else null
    }

    fun DownloadEntity.toSong(): Song = Song(
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

    private fun DownloadEntity.toCacheEntity() = SongCacheEntity(
        songId = songId,
        title = title,
        artist = artist,
        album = album,
        coverUrl = coverUrl,
        mediaUrl = mediaUrl,
        durationMs = durationMs,
        source = SongSource.LOCAL.name,
        filePath = localPath,
    )

    private fun updateProgress(songId: String, progress: Float, isDownloading: Boolean) {
        _progress.value = _progress.value + (songId to DownloadProgress(songId, progress, isDownloading))
        if (!isDownloading) _progress.value = _progress.value - songId
    }
}
