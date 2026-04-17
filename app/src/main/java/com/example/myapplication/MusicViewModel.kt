package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.myapplication.data.HomeState
import com.example.myapplication.data.MusicRepository
import com.example.myapplication.data.PlayerState
import com.example.myapplication.data.RepeatMode
import com.example.myapplication.data.Song
import com.example.myapplication.service.MusicService
import com.example.myapplication.service.SleepTimer
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicViewModel(private val context: Context) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _favoriteSongIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteSongIds: StateFlow<Set<String>> = _favoriteSongIds.asStateFlow()

    private var mediaController: MediaController? = null
    private var isPositionTracking = false

    private val sleepTimer = SleepTimer()

    init {
        connectToService()
        loadHomeData()
        observeSleepTimer()
    }

    private fun observeSleepTimer() {
        viewModelScope.launch {
            sleepTimer.remainingMs.collect { remaining ->
                _playerState.update {
                    it.copy(
                        sleepTimerRemainingMs = remaining,
                        isSleepTimerActive = remaining > 0,
                    )
                }
            }
        }
    }

    fun startSleepTimer(durationMs: Long) {
        sleepTimer.start(durationMs, viewModelScope) {
            mediaController?.pause()
        }
    }

    fun cancelSleepTimer() {
        sleepTimer.cancel()
    }

    private fun loadHomeData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _homeState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    error = null,
                )
            }
            try {
                val trending = async { MusicRepository.getTrendingTamil() }
                val recent = async { MusicRepository.getRecentTamil() }
                val romantic = async { MusicRepository.getTamilRomantic() }
                val mass = async { MusicRepository.getTamilMass() }
                val anirudh = async { MusicRepository.getAnirudhHits() }
                val arRahman = async { MusicRepository.getARRahmanHits() }

                _homeState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        trendingSongs = trending.await(),
                        recentSongs = recent.await(),
                        romanticSongs = romantic.await(),
                        massSongs = mass.await(),
                        anirudhHits = anirudh.await(),
                        arRahmanHits = arRahman.await(),
                    )
                }
            } catch (e: Exception) {
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Failed to load songs. Check your connection.",
                    )
                }
            }
        }
    }

    fun refreshHome() {
        loadHomeData(isRefresh = true)
    }

    fun searchSongs(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = MusicRepository.searchSongs(query)
            } catch (_: Exception) {
                _searchResults.value = emptyList()
            }
            _isSearching.value = false
        }
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startPositionTracking() else stopPositionTracking()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSong()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _playerState.update {
                        it.copy(duration = mediaController?.duration ?: 0L)
                    }
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playerState.update { it.copy(shuffleEnabled = shuffleModeEnabled) }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                _playerState.update { it.copy(repeatMode = mode) }
            }
        })
    }

    private fun updateCurrentSong() {
        val controller = mediaController ?: return
        val currentMediaItem = controller.currentMediaItem ?: return
        val songId = currentMediaItem.mediaId
        val song = _playerState.value.playlist.find { it.id == songId }
            ?: _homeState.value.allSongs.find { it.id == songId }
        _playerState.update {
            it.copy(
                currentSong = song,
                currentIndex = controller.currentMediaItemIndex,
                duration = controller.duration.coerceAtLeast(0L),
            )
        }
    }

    private fun startPositionTracking() {
        if (isPositionTracking) return
        isPositionTracking = true
        viewModelScope.launch {
            while (isPositionTracking) {
                val position = mediaController?.currentPosition ?: 0L
                val duration = mediaController?.duration ?: 0L
                _playerState.update {
                    it.copy(
                        currentPosition = position,
                        duration = duration.coerceAtLeast(0L),
                    )
                }
                delay(500)
            }
        }
    }

    private fun stopPositionTracking() {
        isPositionTracking = false
    }

    fun playSong(song: Song, playlist: List<Song>? = null) {
        val controller = mediaController ?: return
        val effectivePlaylist = playlist ?: _homeState.value.allSongs
        val mediaItems = effectivePlaylist.map { s ->
            MediaItem.Builder()
                .setMediaId(s.id.toString())
                .setUri(s.mediaUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(Uri.parse(s.coverUrl))
                        .build()
                )
                .build()
        }
        val startIndex = effectivePlaylist.indexOf(song).coerceAtLeast(0)
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
        _playerState.update {
            it.copy(
                currentSong = song,
                playlist = effectivePlaylist,
                queue = effectivePlaylist,
                currentIndex = startIndex,
            )
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun skipNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        }
    }

    fun skipPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun toggleFavorite(songId: String) {
        _favoriteSongIds.update { current ->
            if (songId in current) current - songId else current + songId
        }
    }

    fun isFavorite(songId: String): Boolean = songId in _favoriteSongIds.value

    fun toggleRepeatMode() {
        val controller = mediaController ?: return
        controller.repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    // Queue management
    fun addToQueue(song: Song) {
        val controller = mediaController ?: return
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.mediaUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(Uri.parse(song.coverUrl))
                    .build()
            )
            .build()
        controller.addMediaItem(mediaItem)
        _playerState.update { it.copy(queue = it.queue + song) }
    }

    fun playNext(song: Song) {
        val controller = mediaController ?: return
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.mediaUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(Uri.parse(song.coverUrl))
                    .build()
            )
            .build()
        val insertIndex = controller.currentMediaItemIndex + 1
        controller.addMediaItem(insertIndex, mediaItem)
        _playerState.update {
            val mutableQueue = it.queue.toMutableList()
            val queueInsertIndex = (it.currentIndex + 1).coerceAtMost(mutableQueue.size)
            mutableQueue.add(queueInsertIndex, song)
            it.copy(queue = mutableQueue)
        }
    }

    fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        if (index >= 0 && index < controller.mediaItemCount) {
            controller.removeMediaItem(index)
            _playerState.update {
                val mutableQueue = it.queue.toMutableList()
                if (index < mutableQueue.size) mutableQueue.removeAt(index)
                it.copy(queue = mutableQueue)
            }
        }
    }

    fun clearQueue() {
        val controller = mediaController ?: return
        val currentIndex = controller.currentMediaItemIndex
        // Keep current item, remove everything else
        for (i in controller.mediaItemCount - 1 downTo 0) {
            if (i != currentIndex) controller.removeMediaItem(i)
        }
        val currentSong = _playerState.value.currentSong
        _playerState.update {
            it.copy(queue = if (currentSong != null) listOf(currentSong) else emptyList())
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionTracking()
        sleepTimer.cancel()
        mediaController?.release()
    }
}

class MusicViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MusicViewModel(context.applicationContext) as T
    }
}
