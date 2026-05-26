package com.example.myapplication

import android.content.Intent
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.LyricsState
import com.example.myapplication.data.Song
import com.example.myapplication.data.isPlayable
import com.example.myapplication.data.mergeCatalogSongs
import com.example.myapplication.ui.components.syncOrderedListByIds
import com.example.myapplication.ui.components.AddSongsToPlaylistSheet
import com.example.myapplication.ui.components.AddToPlaylistDialog
import com.example.myapplication.ui.components.BottomNavBar
import com.example.myapplication.ui.components.CreatePlaylistDialog
import com.example.myapplication.ui.components.HeartBurstOverlay
import com.example.myapplication.ui.components.MiniPlayer
import com.example.myapplication.ui.components.SleepTimerDialog
import com.example.myapplication.ui.components.SpotlightOnboarding
import com.example.myapplication.ui.components.SpotlightTarget
import com.example.myapplication.ui.screens.DownloadsScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.LibraryScreen
import com.example.myapplication.ui.screens.LyricsScreen
import com.example.myapplication.ui.screens.PlayerScreen
import com.example.myapplication.ui.screens.PlaylistDetailScreen
import com.example.myapplication.ui.screens.QueueScreen
import com.example.myapplication.ui.screens.SearchScreen
import com.example.myapplication.ui.screens.SettingsScreen
import com.example.myapplication.ui.screens.SplashScreen
import com.example.myapplication.ui.theme.DVibessTheme
import com.example.myapplication.viewmodel.PlaylistViewModel
import com.example.myapplication.viewmodel.SettingsState
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_PLAYER = "extra_open_player"
    }

    private var openPlayerTrigger = mutableIntStateOf(0)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        handleLaunchIntent(intent)
        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()
            val playerLaunchTrigger by openPlayerTrigger

            DVibessTheme(
                appTheme = settingsState.appTheme,
                useDynamicColor = settingsState.useDynamicColor,
                accentColor = if (!settingsState.useDynamicColor) Color(settingsState.accentColor) else null,
                useAuroraTheme = settingsState.useAuroraTheme,
            ) {
                DVibessApp(
                    settingsViewModel = settingsViewModel,
                    settingsState = settingsState,
                    openPlayerTrigger = playerLaunchTrigger,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_PLAYER, false) == true) {
            openPlayerTrigger.intValue++
            intent.removeExtra(EXTRA_OPEN_PLAYER)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun DVibessApp(
    settingsViewModel: SettingsViewModel,
    settingsState: SettingsState,
    openPlayerTrigger: Int = 0,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val musicViewModel: MusicViewModel = koinViewModel()
    val playlistViewModel: PlaylistViewModel = koinViewModel()

    val playerState by musicViewModel.playerState.collectAsStateWithLifecycle()
    val homeState by musicViewModel.homeState.collectAsStateWithLifecycle()
    val searchResults by musicViewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by musicViewModel.isSearching.collectAsStateWithLifecycle()
    val favoriteIds by playlistViewModel.favoriteIds.collectAsStateWithLifecycle()
    val favoriteSongIds = remember(favoriteIds) { favoriteIds.toSet() }
    val playlists by playlistViewModel.playlists.collectAsStateWithLifecycle()
    val playlistSongCounts by playlistViewModel.playlistSongCounts.collectAsStateWithLifecycle()
    val favoriteSongs by playlistViewModel.favoriteSongs.collectAsStateWithLifecycle()
    val downloadedSongs by playlistViewModel.downloadedSongs.collectAsStateWithLifecycle()
    val downloadedIds by playlistViewModel.downloadedIds.collectAsStateWithLifecycle()
    val downloadProgress by playlistViewModel.downloadProgress.collectAsStateWithLifecycle()

    var selectedNavIndex by rememberSaveable { mutableIntStateOf(0) }
    var showPlayerScreen by rememberSaveable { mutableStateOf(false) }
    var showQueueScreen by rememberSaveable { mutableStateOf(false) }
    var showLyricsScreen by rememberSaveable { mutableStateOf(false) }
    var showDownloadsScreen by rememberSaveable { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSaveQueueDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showAddSongsSheet by remember { mutableStateOf(false) }
    var pendingSongForPlaylist by remember { mutableStateOf<Song?>(null) }
    var selectedPlaylistId by rememberSaveable { mutableStateOf<Long?>(null) }
    var playlistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var playlistRefreshTrigger by remember { mutableIntStateOf(0) }
    var heartBurstTrigger by remember { mutableIntStateOf(0) }

    val selectedPlaylist = remember(playlists, selectedPlaylistId) {
        selectedPlaylistId?.let { id -> playlists.find { it.id == id } }
    }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    val prefs = remember { context.getSharedPreferences("dvibess_prefs", Context.MODE_PRIVATE) }
    var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarding_complete", false)) }
    val navItemBounds = remember { mutableMapOf<Int, Rect>() }
    var contentBounds by remember { mutableStateOf<Rect?>(null) }

    val catalogSongs = remember(homeState.allSongs, downloadedSongs, searchResults) {
        mergeCatalogSongs(homeState.allSongs, searchResults, downloadedSongs)
    }

    LaunchedEffect(selectedPlaylist?.id, catalogSongs, playlistRefreshTrigger, downloadedIds) {
        val playlist = selectedPlaylist
        if (playlist == null) {
            playlistSongs = emptyList()
        } else {
            val loaded = playlistViewModel.getPlaylistSongs(playlist.id, catalogSongs)
            playlistSongs = syncOrderedListByIds(playlistSongs, loaded) { it.id }
        }
    }

    val openAddToPlaylistDialog: (Song) -> Unit = remember {
        { song ->
            pendingSongForPlaylist = song
            showAddToPlaylistDialog = true
        }
    }

    val onSongClick: (Song, List<Song>) -> Unit = remember(musicViewModel, playlistViewModel) {
        { song, playlist ->
            scope.launch {
                if (!song.isPlayable()) return@launch
                val enriched = playlistViewModel.enrichWithDownloads(playlist)
                val enrichedSong = enriched.find { it.id == song.id } ?: song
                if (!enrichedSong.isPlayable()) return@launch
                musicViewModel.playSong(enrichedSong, enriched, sequential = true) { started ->
                    playlistViewModel.logPlay(started)
                }
            }
        }
    }

    val onPlaylistShuffle: () -> Unit = remember(musicViewModel, playlistViewModel) {
        {
            scope.launch {
                if (playlistSongs.isEmpty()) return@launch
                val enriched = playlistViewModel.enrichWithDownloads(playlistSongs)
                musicViewModel.playShuffled(enriched) { started ->
                    playlistViewModel.logPlay(started)
                }
            }
        }
    }

    var pendingOpenPlayer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        musicViewModel.syncPlaybackState()
    }

    LaunchedEffect(openPlayerTrigger) {
        if (openPlayerTrigger > 0) {
            showSplash = false
            pendingOpenPlayer = true
            musicViewModel.syncPlaybackState()
        }
    }

    LaunchedEffect(pendingOpenPlayer, playerState.currentSong) {
        if (pendingOpenPlayer && playerState.currentSong != null) {
            showPlayerScreen = true
            pendingOpenPlayer = false
        }
    }

    LaunchedEffect(playerState.currentSong) {
        if (playerState.currentSong != null) {
            showSplash = false
        }
    }

    LaunchedEffect(showPlayerScreen, playerState.currentSong, pendingOpenPlayer) {
        if (showPlayerScreen && playerState.currentSong == null && !pendingOpenPlayer) {
            showPlayerScreen = false
        }
    }

    val showMiniPlayer = playerState.currentSong != null && !showPlayerScreen && !showLyricsScreen && !showQueueScreen
    val showBottomNav = selectedPlaylist == null && !showDownloadsScreen && !showPlayerScreen && !showLyricsScreen && !showQueueScreen

    if (showSplash && playerState.currentSong == null && openPlayerTrigger == 0) {
        SplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    if (showSaveQueueDialog) {
        CreatePlaylistDialog(
            onDismiss = { showSaveQueueDialog = false },
            onCreate = { name ->
                playlistViewModel.saveQueueAsPlaylist(name, playerState.queue)
                showSaveQueueDialog = false
            },
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = {
                showCreatePlaylistDialog = false
            },
            onCreate = { name ->
                val pending = pendingSongForPlaylist
                if (pending != null) {
                    playlistViewModel.createPlaylist(name) { id ->
                        playlistViewModel.addSongToPlaylist(id, pending)
                        pendingSongForPlaylist = null
                    }
                } else {
                    playlistViewModel.createPlaylist(name)
                }
                showCreatePlaylistDialog = false
            },
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = {
                showAddToPlaylistDialog = false
                pendingSongForPlaylist = null
            },
            onSelectPlaylist = { playlistId ->
                pendingSongForPlaylist?.let { song ->
                    playlistViewModel.addSongToPlaylist(playlistId, song) {
                        if (selectedPlaylist?.id == playlistId) {
                            playlistRefreshTrigger++
                        }
                    }
                }
                showAddToPlaylistDialog = false
                pendingSongForPlaylist = null
            },
            onCreateNew = {
                showAddToPlaylistDialog = false
                showCreatePlaylistDialog = true
            },
        )
    }

    if (showAddSongsSheet) {
        selectedPlaylist?.let { playlist ->
            AddSongsToPlaylistSheet(
                playlistName = playlist.name,
                songs = catalogSongs,
                existingSongIds = playlistSongs.map { it.id }.toSet(),
                onDismiss = { showAddSongsSheet = false },
                onAddSong = { song ->
                    playlistViewModel.addSongToPlaylist(playlist.id, song) {
                        playlistRefreshTrigger++
                    }
                },
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            selectedPlaylist != null -> {
                val playlist = selectedPlaylist
                PlaylistDetailScreen(
                    playlistName = playlist.name,
                    songs = playlistSongs,
                    downloadedSongIds = downloadedIds,
                    downloadProgress = downloadProgress,
                    currentSongId = playerState.currentSong?.id,
                    isPlaying = playerState.isPlaying,
                    onBack = { selectedPlaylistId = null },
                    onSongClick = onSongClick,
                    onShuffle = onPlaylistShuffle,
                    onDownloadSong = playlistViewModel::downloadSong,
                    onRename = { newName ->
                        playlistViewModel.renamePlaylist(playlist.id, newName)
                    },
                    onDelete = {
                        playlistViewModel.deletePlaylist(playlist.id)
                        selectedPlaylistId = null
                    },
                    onAddSongs = { showAddSongsSheet = true },
                    onRemoveSong = { song ->
                        playlistViewModel.removeSongFromPlaylist(playlist.id, song.id)
                        playlistRefreshTrigger++
                    },
                    onReorderSongs = { reordered ->
                        playlistSongs = reordered
                        playlistViewModel.reorderPlaylistSongs(playlist.id, reordered)
                    },
                )
            }
            showDownloadsScreen -> {
                DownloadsScreen(
                    songs = downloadedSongs,
                    currentSongId = playerState.currentSong?.id,
                    isPlaying = playerState.isPlaying,
                    onBack = { showDownloadsScreen = false },
                    onSongClick = onSongClick,
                    onDeleteDownload = playlistViewModel::deleteDownload,
                )
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coords -> contentBounds = coords.boundsInRoot() },
                    ) {
                        when (selectedNavIndex) {
                            0 -> HomeScreen(
                                homeState = homeState,
                                onSongClick = onSongClick,
                                onRefresh = musicViewModel::refreshHome,
                                currentSongId = playerState.currentSong?.id,
                                isPlaying = playerState.isPlaying,
                                isAuroraTheme = settingsState.useAuroraTheme,
                                onToggleAurora = {
                                    settingsViewModel.setUseAuroraTheme(!settingsState.useAuroraTheme)
                                },
                                modifier = Modifier.padding(top = 48.dp),
                            )
                            1 -> SearchScreen(
                                searchResults = searchResults,
                                isSearching = isSearching,
                                onSearch = musicViewModel::searchSongs,
                                onSongClick = onSongClick,
                                onAddToPlaylist = openAddToPlaylistDialog,
                                currentSongId = playerState.currentSong?.id,
                                isPlaying = playerState.isPlaying,
                                isAuroraTheme = settingsState.useAuroraTheme,
                                modifier = Modifier.padding(top = 48.dp),
                            )
                            2 -> LibraryScreen(
                                songs = catalogSongs,
                                playlists = playlists,
                                playlistSongCounts = playlistSongCounts,
                                favoriteSongs = favoriteSongs,
                                downloadedSongs = downloadedSongs,
                                downloadedSongIds = downloadedIds,
                                downloadProgress = downloadProgress,
                                onSongClick = onSongClick,
                                onCreatePlaylist = { name, onDone ->
                                    playlistViewModel.createPlaylist(name) { id ->
                                        selectedPlaylistId = id
                                        showAddSongsSheet = true
                                        onDone()
                                    }
                                },
                                onPlaylistClick = { playlist ->
                                    selectedPlaylistId = playlist.id
                                },
                                onAddSongToPlaylist = openAddToPlaylistDialog,
                                onDownloadsClick = { showDownloadsScreen = true },
                                onDownloadSong = playlistViewModel::downloadSong,
                                currentSongId = playerState.currentSong?.id,
                                isPlaying = playerState.isPlaying,
                                isAuroraTheme = settingsState.useAuroraTheme,
                                modifier = Modifier.padding(top = 48.dp),
                            )
                            3 -> SettingsScreen(
                                settingsState = settingsState,
                                onBack = null,
                                onSetTheme = settingsViewModel::setAppTheme,
                                onSetAccentColor = settingsViewModel::setAccentColor,
                                onSetDynamicColor = settingsViewModel::setUseDynamicColor,
                                onSetNowPlayingTheme = settingsViewModel::setNowPlayingTheme,
                                onSetCrossfade = settingsViewModel::setCrossfadeEnabled,
                                onSetCrossfadeDuration = settingsViewModel::setCrossfadeDuration,
                                onSetMinDuration = settingsViewModel::setMinDurationFilter,
                                onSetAutoLyrics = settingsViewModel::setAutoFetchLyrics,
                                onSetGapless = settingsViewModel::setGaplessPlayback,
                                onClearHistory = playlistViewModel::clearHistory,
                                modifier = Modifier.padding(top = 48.dp),
                            )
                        }
                    }
                }
            }
        }

        if (showQueueScreen) {
            QueueScreen(
                queue = playerState.queue,
                currentIndex = if (playerState.shuffleEnabled) {
                    playerState.queue.indexOfFirst { it.id == playerState.currentSong?.id }.coerceAtLeast(0)
                } else {
                    playerState.currentIndex
                },
                shuffleEnabled = playerState.shuffleEnabled,
                onBack = { showQueueScreen = false },
                onSongClick = { index -> musicViewModel.playQueueItem(index) },
                onRemove = musicViewModel::removeFromQueue,
                onMove = musicViewModel::moveQueueItem,
                onClearQueue = musicViewModel::clearQueue,
                onSaveAsPlaylist = { showSaveQueueDialog = true },
            )
        }

        if (showLyricsScreen && playerState.currentSong != null) {
            LyricsScreen(
                lyricsState = LyricsState(),
                songTitle = playerState.currentSong?.title ?: "",
                songArtist = playerState.currentSong?.artist ?: "",
                onDismiss = { showLyricsScreen = false },
                onSeekToLine = { musicViewModel.seekTo(it) },
            )
        }

        if (showPlayerScreen && playerState.currentSong != null) {
            PlayerScreen(
                playerState = playerState,
                isFavorite = playerState.currentSong?.id in favoriteSongIds,
                onBackClick = { showPlayerScreen = false },
                onPlayPause = musicViewModel::togglePlayPause,
                onSkipNext = musicViewModel::skipNext,
                onSkipPrevious = musicViewModel::skipPrevious,
                onSeek = musicViewModel::seekTo,
                onToggleShuffle = musicViewModel::toggleShuffle,
                onToggleRepeat = musicViewModel::toggleRepeatMode,
                onToggleFavorite = {
                    playerState.currentSong?.let { song ->
                        if (song.id !in favoriteSongIds) heartBurstTrigger++
                        playlistViewModel.toggleFavorite(song)
                    }
                },
                nowPlayingTheme = settingsState.nowPlayingTheme,
                onShowLyrics = { showLyricsScreen = true },
                onShowQueue = { showQueueScreen = true },
                onShowSleepTimer = { showSleepTimerDialog = true },
                isAuroraTheme = settingsState.useAuroraTheme,
                isDownloaded = playerState.currentSong?.id in downloadedIds,
                isDownloading = playerState.currentSong?.let {
                    downloadProgress[it.id]?.isDownloading == true
                } == true,
                onDownload = { playerState.currentSong?.let { playlistViewModel.downloadSong(it) } },
                onAddToPlaylist = {
                    playerState.currentSong?.let(openAddToPlaylistDialog)
                },
            )
        }

        if (showMiniPlayer || showBottomNav) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                AnimatedVisibility(
                    visible = showMiniPlayer,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    MiniPlayer(
                        playerState = playerState,
                        onPlayPause = musicViewModel::togglePlayPause,
                        onSkipNext = musicViewModel::skipNext,
                        onClick = { showPlayerScreen = true },
                        isFavorite = playerState.currentSong?.id in favoriteSongIds,
                        onToggleFavorite = {
                            playerState.currentSong?.let { song ->
                                if (song.id !in favoriteSongIds) heartBurstTrigger++
                                playlistViewModel.toggleFavorite(song)
                            }
                        },
                    )
                }
                if (showBottomNav) {
                    BottomNavBar(
                        selectedIndex = selectedNavIndex,
                        onItemSelected = { selectedNavIndex = it },
                        onItemPositioned = { index, coords ->
                            navItemBounds[index] = coords.boundsInRoot()
                        },
                    )
                }
            }
        }

        if (showSleepTimerDialog) {
            SleepTimerDialog(
                isTimerActive = playerState.isSleepTimerActive,
                remainingMs = playerState.sleepTimerRemainingMs,
                onDismiss = { showSleepTimerDialog = false },
                onSetTimer = { durationMs ->
                    musicViewModel.startSleepTimer(durationMs)
                    showSleepTimerDialog = false
                },
                onCancelTimer = {
                    musicViewModel.cancelSleepTimer()
                    showSleepTimerDialog = false
                },
            )
        }

        HeartBurstOverlay(trigger = heartBurstTrigger)

        if (showOnboarding && navItemBounds.size >= 3 && contentBounds != null) {
            SpotlightOnboarding(
                targets = buildSpotlightTargets(navItemBounds, contentBounds!!),
                onFinish = {
                    showOnboarding = false
                    prefs.edit().putBoolean("onboarding_complete", true).apply()
                },
            )
        }
    }
}

private fun buildSpotlightTargets(
    navBounds: Map<Int, Rect>,
    contentBounds: Rect,
): List<SpotlightTarget> = listOf(
    SpotlightTarget(
        bounds = contentBounds.copy(
            top = contentBounds.top + 80f,
            bottom = contentBounds.top + 280f,
            left = contentBounds.left + 40f,
            right = contentBounds.right - 40f,
        ),
        title = "Welcome to DVibess \uD83C\uDFB5",
        description = "Built by AK Works\nDiscover Tamil music, create playlists, and download songs offline.",
        tooltipAbove = false,
    ),
    SpotlightTarget(
        bounds = navBounds[0]!!,
        title = "Home",
        description = "Your music feed — trending tracks and personalized picks.",
        tooltipAbove = true,
    ),
    SpotlightTarget(
        bounds = navBounds[1]!!,
        title = "Search",
        description = "Find any song, artist, or album.",
        tooltipAbove = true,
    ),
    SpotlightTarget(
        bounds = navBounds[2]!!,
        title = "Library",
        description = "Playlists, downloads, and favorites — all in one place.",
        tooltipAbove = true,
    ),
)
