package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Song
import com.example.myapplication.data.local.LocalAlbum
import com.example.myapplication.data.local.LocalArtist
import com.example.myapplication.data.local.LocalGenre
import com.example.myapplication.data.local.LocalMusicScanner
import com.example.myapplication.data.preferences.AppPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryState(
    val localSongs: List<Song> = emptyList(),
    val albums: List<LocalAlbum> = emptyList(),
    val artists: List<LocalArtist> = emptyList(),
    val genres: List<LocalGenre> = emptyList(),
    val folders: Map<String, List<Song>> = emptyMap(),
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false,
)

class LibraryViewModel(
    private val scanner: LocalMusicScanner,
    private val preferences: AppPreferences,
) : ViewModel() {

    private val _libraryState = MutableStateFlow(LibraryState())
    val libraryState: StateFlow<LibraryState> = _libraryState.asStateFlow()

    fun setPermissionGranted(granted: Boolean) {
        _libraryState.update { it.copy(hasPermission = granted) }
        if (granted) loadLocalMusic()
    }

    fun loadLocalMusic() {
        viewModelScope.launch {
            _libraryState.update { it.copy(isLoading = true) }

            val minDuration = preferences.minDurationFilter.first()
            val songs = scanner.scanSongs(minDuration)
            val albums = scanner.scanAlbums()
            val artists = scanner.scanArtists()
            val genres = scanner.scanGenres()
            val folders = scanner.getSongsByFolder(songs)

            _libraryState.update {
                it.copy(
                    localSongs = songs,
                    albums = albums,
                    artists = artists,
                    genres = genres,
                    folders = folders,
                    isLoading = false,
                )
            }
        }
    }

    fun getSongsByAlbum(albumId: Long): List<Song> {
        return _libraryState.value.localSongs.filter { it.albumId == albumId }
    }

    fun getSongsByArtist(artistId: Long): List<Song> {
        return _libraryState.value.localSongs.filter { it.artistId == artistId }
    }

    fun getSongsByFolder(folderPath: String): List<Song> {
        return _libraryState.value.folders[folderPath] ?: emptyList()
    }
}
