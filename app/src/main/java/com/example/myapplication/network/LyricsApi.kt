package com.example.myapplication.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

@Serializable
data class LyricsResponse(
    val lyrics: String = "",
)

class LyricsApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchLyrics(title: String, artist: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val encodedArtist = URLEncoder.encode(artist.split(",").first().trim(), "UTF-8")
                val encodedTitle = URLEncoder.encode(title, "UTF-8")
                val url = "https://api.lyrics.ovh/v1/$encodedArtist/$encodedTitle"

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext ""

                if (response.isSuccessful) {
                    json.decodeFromString<LyricsResponse>(body).lyrics
                } else ""
            } catch (_: Exception) {
                ""
            }
        }
    }
}
