package com.example.myapplication.data

import com.example.myapplication.network.MusicApi

object MusicRepository {

    private suspend fun searchAndMap(query: String, limit: Int = 15): List<Song> {
        val response = MusicApi.search(query, limit)
        return response.results
            .map { it.toSong() }
            .filter { it.mediaUrl.isNotBlank() }
    }

    private fun normalizeSearchQuery(query: String): String {
        val lower = query.trim().lowercase()
        val isKavasamSearch = lower.contains("kavasam") || lower.contains("kavacham") ||
            lower.contains("sasti") || lower.contains("sashti") || lower.contains("shasti")

        if (!isKavasamSearch) return query.trim()

        return when {
            lower.contains("kandha") || lower.contains("kanda") || lower.contains("kandar") -> query.trim()
            lower.contains("shanth") || lower.contains("shanda") || lower.contains("shasta") ->
                query.trim().replace(Regex("shanth[a]?|shanda|shasta", RegexOption.IGNORE_CASE), "Kandha")
            else -> "Kandha Sasti Kavasam"
        }
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

    suspend fun getDevotional(): List<Song> =
        searchAndMap("Kandha Sasti Kavasam", 10)

    suspend fun searchSongs(query: String): List<Song> {
        val normalized = normalizeSearchQuery(query)
        val results = searchAndMap(normalized, 30)
        if (results.isNotEmpty() || normalized == query.trim()) return results
        return searchAndMap(query.trim(), 30)
    }
}
