package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.LyricsState
import com.example.myapplication.data.lyrics.LyricsRepository
import com.example.myapplication.data.preferences.AppPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LyricsViewModel(
    private val lyricsRepository: LyricsRepository,
    private val preferences: AppPreferences,
) : ViewModel() {

    private val _lyricsState = MutableStateFlow(LyricsState())
    val lyricsState: StateFlow<LyricsState> = _lyricsState.asStateFlow()

    private var currentSongId: String? = null

    fun loadLyrics(songId: String, title: String, artist: String) {
        if (songId == currentSongId) return
        currentSongId = songId

        viewModelScope.launch {
            _lyricsState.update { it.copy(isLoading = true) }

            val autoFetch = preferences.autoFetchLyrics.first()
            if (!autoFetch) {
                _lyricsState.update { it.copy(isLoading = false) }
                return@launch
            }

            val (plain, synced) = lyricsRepository.getLyrics(songId, title, artist)

            _lyricsState.update {
                it.copy(
                    lyrics = plain,
                    syncedLyrics = synced,
                    isLoading = false,
                    hasSyncedLyrics = synced.isNotEmpty(),
                )
            }
        }
    }

    fun updateCurrentLine(positionMs: Long) {
        val synced = _lyricsState.value.syncedLyrics
        if (synced.isEmpty()) return

        val index = synced.indexOfLast { it.timestampMs <= positionMs }
        if (index != _lyricsState.value.currentLineIndex) {
            _lyricsState.update { it.copy(currentLineIndex = index) }
        }
    }

    fun clearLyrics() {
        currentSongId = null
        _lyricsState.value = LyricsState()
    }
}
