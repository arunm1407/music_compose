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
import com.example.myapplication.data.SongSource
import com.example.myapplication.data.isPlayable
import com.example.myapplication.data.playbackUri
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
    private val pendingControllerActions = mutableListOf<() -> Unit>()

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
                val devotional = async { MusicRepository.getDevotional() }

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
                        devotionalSongs = devotional.await(),
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
            syncPlaybackState()
            flushPendingControllerActions()
        }, MoreExecutors.directExecutor())
    }

    private fun runWhenControllerReady(action: () -> Unit) {
        val controller = mediaController
        if (controller != null) {
            action()
        } else {
            pendingControllerActions.add(action)
        }
    }

    private fun flushPendingControllerActions() {
        val actions = pendingControllerActions.toList()
        pendingControllerActions.clear()
        actions.forEach { it() }
    }

    fun syncPlaybackState() {
        val controller = mediaController ?: return
        if (controller.mediaItemCount == 0) {
            _playerState.update {
                it.copy(
                    isPlaying = controller.isPlaying,
                    currentPosition = controller.currentPosition.coerceAtLeast(0L),
                    duration = controller.duration.coerceAtLeast(0L),
                    shuffleEnabled = controller.shuffleModeEnabled,
                    repeatMode = mapRepeatMode(controller.repeatMode),
                )
            }
            if (controller.isPlaying) startPositionTracking()
            return
        }

        val queue = buildList {
            for (index in 0 until controller.mediaItemCount) {
                controller.getMediaItemAt(index)?.let { mediaItemToSong(it) }?.let { add(it) }
            }
        }
        val currentIndex = controller.currentMediaItemIndex.coerceIn(0, (queue.size - 1).coerceAtLeast(0))
        _playerState.update {
            it.copy(
                currentSong = queue.getOrNull(currentIndex),
                queue = queue,
                playlist = queue,
                currentIndex = currentIndex,
                isPlaying = controller.isPlaying,
                currentPosition = controller.currentPosition.coerceAtLeast(0L),
                duration = controller.duration.coerceAtLeast(0L),
                shuffleEnabled = controller.shuffleModeEnabled,
                repeatMode = mapRepeatMode(controller.repeatMode),
            )
        }
        if (controller.isPlaying) startPositionTracking()
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
                _playerState.update { it.copy(repeatMode = mapRepeatMode(repeatMode)) }
            }
        })
    }

    private fun mapRepeatMode(repeatMode: Int): RepeatMode {
        return when (repeatMode) {
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }
    }

    private fun mediaItemToSong(item: MediaItem): Song {
        val uri = item.localConfiguration?.uri?.toString().orEmpty()
        val isLocalFile = uri.startsWith("file:")
        return Song(
            id = item.mediaId,
            title = item.mediaMetadata.title?.toString().orEmpty().ifBlank { "Unknown" },
            artist = item.mediaMetadata.artist?.toString().orEmpty(),
            album = item.mediaMetadata.albumTitle?.toString().orEmpty(),
            coverUrl = item.mediaMetadata.artworkUri?.toString().orEmpty(),
            mediaUrl = if (isLocalFile) "" else uri,
            filePath = if (isLocalFile) Uri.parse(uri).path else null,
            source = if (isLocalFile) SongSource.LOCAL else SongSource.STREAM,
        )
    }

    private fun updateCurrentSong() {
        val controller = mediaController ?: return
        val currentMediaItem = controller.currentMediaItem ?: return
        val songId = currentMediaItem.mediaId
        val index = controller.currentMediaItemIndex
        val song = _playerState.value.queue.getOrNull(index)?.takeIf { it.id == songId }
            ?: _playerState.value.queue.find { it.id == songId }
            ?: _playerState.value.playlist.find { it.id == songId }
            ?: _homeState.value.allSongs.find { it.id == songId }
            ?: mediaItemToSong(currentMediaItem)
        _playerState.update {
            it.copy(
                currentSong = song,
                currentIndex = index,
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

    fun playSong(
        song: Song,
        playlist: List<Song>? = null,
        sequential: Boolean = true,
        shuffle: Boolean = false,
    ) {
        runWhenControllerReady { playSongInternal(song, playlist, sequential, shuffle) }
    }

    fun playShuffled(playlist: List<Song>) {
        val playable = playlist.filter { it.isPlayable() }
        if (playable.isEmpty()) return
        playSong(
            song = playable.random(),
            playlist = playable,
            sequential = false,
            shuffle = true,
        )
    }

    private fun playSongInternal(
        song: Song,
        playlist: List<Song>?,
        sequential: Boolean,
        shuffle: Boolean,
    ) {
        val controller = mediaController ?: return
        val sourcePlaylist = playlist ?: _homeState.value.allSongs
        var effectivePlaylist = sourcePlaylist.filter { it.isPlayable() }

        if (effectivePlaylist.isEmpty()) {
            if (!song.isPlayable()) return
            effectivePlaylist = listOf(song)
        } else if (effectivePlaylist.none { it.id == song.id } && song.isPlayable()) {
            effectivePlaylist = listOf(song) + effectivePlaylist.filter { it.id != song.id }
        }

        val startIndex = effectivePlaylist.indexOfFirst { it.id == song.id }.let { index ->
            if (index >= 0) index else 0
        }
        val resolvedSong = effectivePlaylist[startIndex]

        controller.shuffleModeEnabled = shuffle
        if (sequential && !shuffle) {
            controller.shuffleModeEnabled = false
        }

        val mediaItems = effectivePlaylist.map { s -> buildMediaItem(s) }
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
        _playerState.update {
            it.copy(
                currentSong = resolvedSong,
                playlist = effectivePlaylist,
                queue = effectivePlaylist,
                currentIndex = startIndex,
                shuffleEnabled = controller.shuffleModeEnabled,
            )
        }
    }

    fun playQueueItem(index: Int) {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            if (index !in 0 until controller.mediaItemCount) return@runWhenControllerReady
            controller.seekToDefaultPosition(index)
            updateCurrentSong()
            if (!controller.isPlaying) controller.play()
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            if (fromIndex !in 0 until controller.mediaItemCount) return@runWhenControllerReady
            if (toIndex !in 0 until controller.mediaItemCount) return@runWhenControllerReady
            if (fromIndex == toIndex) return@runWhenControllerReady
            controller.moveMediaItem(fromIndex, toIndex)
            _playerState.update {
                val queue = it.queue.toMutableList()
                if (fromIndex in queue.indices) {
                    val song = queue.removeAt(fromIndex)
                    queue.add(toIndex.coerceIn(0, queue.size), song)
                }
                it.copy(
                    queue = queue,
                    currentIndex = controller.currentMediaItemIndex,
                )
            }
            updateCurrentSong()
        }
    }

    private fun buildMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(song.playbackUri())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(Uri.parse(song.coverUrl))
                    .build()
            )
            .build()
    }

    fun togglePlayPause() {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    fun skipNext() {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            if (controller.hasNextMediaItem()) controller.seekToNextMediaItem()
        }
    }

    fun skipPrevious() {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            if (controller.currentPosition > 3000) {
                controller.seekTo(0)
            } else if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun toggleShuffle() {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }

    fun toggleFavorite(songId: String) {
        _favoriteSongIds.update { current ->
            if (songId in current) current - songId else current + songId
        }
    }

    fun isFavorite(songId: String): Boolean = songId in _favoriteSongIds.value

    fun toggleRepeatMode() {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            controller.repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    // Queue management
    fun addToQueue(song: Song) {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            controller.addMediaItem(buildMediaItem(song))
            _playerState.update { it.copy(queue = it.queue + song) }
        }
    }

    fun playNext(song: Song) {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            val insertIndex = controller.currentMediaItemIndex + 1
            controller.addMediaItem(insertIndex, buildMediaItem(song))
            _playerState.update {
                val mutableQueue = it.queue.toMutableList()
                val queueInsertIndex = (it.currentIndex + 1).coerceAtMost(mutableQueue.size)
                mutableQueue.add(queueInsertIndex, song)
                it.copy(queue = mutableQueue)
            }
        }
    }

    fun removeFromQueue(index: Int) {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            if (index >= 0 && index < controller.mediaItemCount) {
                controller.removeMediaItem(index)
                _playerState.update {
                    val mutableQueue = it.queue.toMutableList()
                    if (index < mutableQueue.size) mutableQueue.removeAt(index)
                    it.copy(
                        queue = mutableQueue,
                        currentIndex = controller.currentMediaItemIndex,
                    )
                }
                updateCurrentSong()
            }
        }
    }

    fun clearQueue() {
        runWhenControllerReady {
            val controller = mediaController ?: return@runWhenControllerReady
            val currentIndex = controller.currentMediaItemIndex
            for (i in controller.mediaItemCount - 1 downTo 0) {
                if (i != currentIndex) controller.removeMediaItem(i)
            }
            val currentSong = _playerState.value.currentSong
            _playerState.update {
                it.copy(
                    queue = if (currentSong != null) listOf(currentSong) else emptyList(),
                    currentIndex = 0,
                )
            }
            updateCurrentSong()
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
