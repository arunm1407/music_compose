package com.example.myapplication.data.lyrics

import com.example.myapplication.data.LyricLine
import com.example.myapplication.data.db.dao.LyricsDao
import com.example.myapplication.data.db.entity.LyricsEntity
import com.example.myapplication.network.LyricsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LyricsRepository(
    private val lyricsDao: LyricsDao,
    private val lyricsApi: LyricsApi,
) {
    suspend fun getLyrics(songId: String, title: String, artist: String): Pair<String, List<LyricLine>> {
        return withContext(Dispatchers.IO) {
            // Check cache first
            val cached = lyricsDao.getCachedLyrics(songId)
            if (cached != null && cached.plainLyrics.isNotBlank()) {
                val synced = if (cached.syncedLyrics.isNotBlank()) {
                    LrcParser.parse(cached.syncedLyrics)
                } else emptyList()
                return@withContext Pair(cached.plainLyrics, synced)
            }

            // Fetch from API
            try {
                val result = lyricsApi.fetchLyrics(title, artist)
                if (result.isNotBlank()) {
                    lyricsDao.cacheLyrics(
                        LyricsEntity(
                            songId = songId,
                            plainLyrics = result,
                            syncedLyrics = "",
                        )
                    )
                    Pair(result, emptyList())
                } else {
                    Pair("", emptyList())
                }
            } catch (_: Exception) {
                Pair("", emptyList())
            }
        }
    }
}
