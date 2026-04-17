package com.example.myapplication.data

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
    val sleepTimerRemainingMs: Long = 0L,
    val isSleepTimerActive: Boolean = false,
)

enum class RepeatMode {
    OFF, ONE, ALL
}

data class LyricLine(
    val timestampMs: Long,
    val text: String,
)

data class LyricsState(
    val lyrics: String = "",
    val syncedLyrics: List<LyricLine> = emptyList(),
    val isLoading: Boolean = false,
    val hasSyncedLyrics: Boolean = false,
    val currentLineIndex: Int = -1,
)
