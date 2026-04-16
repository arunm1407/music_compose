package com.example.myapplication

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.Song
import com.example.myapplication.data.preferences.NowPlayingTheme
import com.example.myapplication.ui.components.BottomNavBar
import com.example.myapplication.ui.components.MiniPlayer
import com.example.myapplication.ui.components.SleepTimerDialog
import com.example.myapplication.ui.components.SpotlightOnboarding
import com.example.myapplication.ui.components.SpotlightTarget
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.LibraryScreen
import com.example.myapplication.ui.screens.LyricsScreen
import com.example.myapplication.ui.screens.PlayerScreen
import com.example.myapplication.ui.screens.QueueScreen
import com.example.myapplication.ui.screens.SearchScreen
import com.example.myapplication.ui.screens.SettingsScreen
import com.example.myapplication.ui.screens.SplashScreen
import com.example.myapplication.ui.theme.DVibessTheme
import com.example.myapplication.viewmodel.SettingsState
import com.example.myapplication.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()

            DVibessTheme(
                appTheme = settingsState.appTheme,
                useDynamicColor = settingsState.useDynamicColor,
                accentColor = if (!settingsState.useDynamicColor) Color(settingsState.accentColor) else null,
                useAuroraTheme = settingsState.useAuroraTheme,
            ) {
                DVibessApp(settingsViewModel = settingsViewModel, settingsState = settingsState)
            }
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
) {
    val context = LocalContext.current
    val viewModel: MusicViewModel = viewModel(
        factory = MusicViewModelFactory(context)
    )
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val favoriteSongIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle()

    var selectedNavIndex by rememberSaveable { mutableIntStateOf(0) }
    var showPlayerScreen by rememberSaveable { mutableStateOf(false) }
    var showQueueScreen by rememberSaveable { mutableStateOf(false) }
    var showLyricsScreen by rememberSaveable { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    // Splash + Onboarding state
    var showSplash by remember { mutableStateOf(true) }
    val prefs = remember { context.getSharedPreferences("dvibess_prefs", Context.MODE_PRIVATE) }
    var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarding_complete", false)) }
    val navItemBounds = remember { mutableMapOf<Int, Rect>() }
    var contentBounds by remember { mutableStateOf<Rect?>(null) }

    val onSongClick: (Song, List<Song>) -> Unit = remember(viewModel) { { song, playlist ->
        viewModel.playSong(song, playlist)
    } }

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            showLyricsScreen && playerState.currentSong != null -> {
                LyricsScreen(
                    lyricsState = com.example.myapplication.data.LyricsState(),
                    songTitle = playerState.currentSong?.title ?: "",
                    songArtist = playerState.currentSong?.artist ?: "",
                    onDismiss = { showLyricsScreen = false },
                    onSeekToLine = { viewModel.seekTo(it) },
                )
            }
            showQueueScreen -> {
                QueueScreen(
                    queue = playerState.queue,
                    currentIndex = playerState.currentIndex,
                    onBack = { showQueueScreen = false },
                    onSongClick = { index ->
                        val song = playerState.queue.getOrNull(index)
                        if (song != null) viewModel.playSong(song, playerState.queue)
                    },
                    onRemove = { index -> viewModel.removeFromQueue(index) },
                    onClearQueue = { viewModel.clearQueue() },
                    onSaveAsPlaylist = { },
                )
            }
            showPlayerScreen && playerState.currentSong != null -> {
                PlayerScreen(
                    playerState = playerState,
                    isFavorite = playerState.currentSong?.id in favoriteSongIds,
                    onBackClick = { showPlayerScreen = false },
                    onPlayPause = viewModel::togglePlayPause,
                    onSkipNext = viewModel::skipNext,
                    onSkipPrevious = viewModel::skipPrevious,
                    onSeek = viewModel::seekTo,
                    onToggleShuffle = viewModel::toggleShuffle,
                    onToggleRepeat = viewModel::toggleRepeatMode,
                    onToggleFavorite = {
                        playerState.currentSong?.id?.let { viewModel.toggleFavorite(it) }
                    },
                    nowPlayingTheme = settingsState.nowPlayingTheme,
                    onShowLyrics = { showLyricsScreen = true },
                    onShowQueue = { showQueueScreen = true },
                    onShowSleepTimer = { showSleepTimerDialog = true },
                )
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Main content area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                contentBounds = coords.boundsInRoot()
                            },
                    ) {
                        when (selectedNavIndex) {
                            0 -> HomeScreen(
                                homeState = homeState,
                                onSongClick = onSongClick,
                                onRefresh = viewModel::refreshHome,
                                currentSongId = playerState.currentSong?.id,
                                isPlaying = playerState.isPlaying,
                                modifier = Modifier.padding(top = 48.dp),
                            )
                            1 -> SearchScreen(
                                searchResults = searchResults,
                                isSearching = isSearching,
                                onSearch = viewModel::searchSongs,
                                onSongClick = onSongClick,
                                currentSongId = playerState.currentSong?.id,
                                isPlaying = playerState.isPlaying,
                                modifier = Modifier.padding(top = 48.dp),
                            )
                            2 -> LibraryScreen(
                                songs = homeState.allSongs,
                                onSongClick = onSongClick,
                                currentSongId = playerState.currentSong?.id,
                                isPlaying = playerState.isPlaying,
                                favoriteSongIds = favoriteSongIds,
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
                                onClearHistory = { },
                                modifier = Modifier.padding(top = 48.dp),
                            )
                        }
                    }

                    // Mini player + Bottom nav
                    Column(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        AnimatedVisibility(
                            visible = playerState.currentSong != null,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it }),
                        ) {
                            MiniPlayer(
                                playerState = playerState,
                                onPlayPause = viewModel::togglePlayPause,
                                onSkipNext = viewModel::skipNext,
                                onClick = { showPlayerScreen = true },
                                isFavorite = playerState.currentSong?.id in favoriteSongIds,
                                onToggleFavorite = {
                                    playerState.currentSong?.id?.let { viewModel.toggleFavorite(it) }
                                },
                            )
                        }

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
        }

        // Sleep timer dialog
        if (showSleepTimerDialog) {
            SleepTimerDialog(
                isTimerActive = playerState.isSleepTimerActive,
                remainingMs = playerState.sleepTimerRemainingMs,
                onDismiss = { showSleepTimerDialog = false },
                onSetTimer = { durationMs ->
                    viewModel.startSleepTimer(durationMs)
                    showSleepTimerDialog = false
                },
                onCancelTimer = {
                    viewModel.cancelSleepTimer()
                    showSleepTimerDialog = false
                },
            )
        }

        // Spotlight onboarding overlay
        if (showOnboarding && navItemBounds.size >= 3 && contentBounds != null) {
            val targets = buildSpotlightTargets(navItemBounds, contentBounds!!)
            SpotlightOnboarding(
                targets = targets,
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
        description = "Built by AK Works\nYour personal music companion — discover, search, and vibe to your favorite tracks.",
        tooltipAbove = false,
    ),
    SpotlightTarget(
        bounds = navBounds[0]!!,
        title = "Home",
        description = "Your music feed — trending tracks, new releases, and personalized picks all in one place.",
        tooltipAbove = true,
    ),
    SpotlightTarget(
        bounds = navBounds[1]!!,
        title = "Search",
        description = "Find any song, artist, or album. Just type and discover.",
        tooltipAbove = true,
    ),
    SpotlightTarget(
        bounds = navBounds[2]!!,
        title = "Library",
        description = "Your collection — all your saved songs and favorites, always at hand.",
        tooltipAbove = true,
    ),
)
