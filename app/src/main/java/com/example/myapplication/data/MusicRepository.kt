package com.example.myapplication.data

import com.example.myapplication.network.MusicApi

object MusicRepository {

    private suspend fun searchAndMap(query: String, limit: Int = 15): List<Song> {
        val response = MusicApi.search(query, limit)
        return response.results
            .map { it.toSong() }
            .filter { it.mediaUrl.isNotBlank() }
    }

    suspend fun getTrendingTamil(): List<Song> =
        searchAndMap("tamil hits 2024")

    suspend fun getRecentTamil(): List<Song> =
        searchAndMap("tamil new songs 2025")

    suspend fun getTamilRomantic(): List<Song> =
        searchAndMap("tamil love songs")

    suspend fun getTamilMass(): List<Song> =
        searchAndMap("tamil kuthu mass songs")

    suspend fun getAnirudhHits(): List<Song> =
        searchAndMap("Anirudh Ravichander tamil")

    suspend fun getARRahmanHits(): List<Song> =
        searchAndMap("AR Rahman tamil hits")

    suspend fun searchSongs(query: String): List<Song> =
        searchAndMap(query, 30)
}
