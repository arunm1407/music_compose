package com.example.myapplication.data

data class HomeState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val trendingSongs: List<Song> = emptyList(),
    val recentSongs: List<Song> = emptyList(),
    val romanticSongs: List<Song> = emptyList(),
    val massSongs: List<Song> = emptyList(),
    val anirudhHits: List<Song> = emptyList(),
    val arRahmanHits: List<Song> = emptyList(),
    val error: String? = null,
) {
    val quickAccess: List<Song>
        get() = trendingSongs.take(6)

    val allSongs: List<Song>
        get() = (trendingSongs + recentSongs + romanticSongs + massSongs + anirudhHits + arRahmanHits)
            .distinctBy { it.id }
}
