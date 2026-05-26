package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.rememberImagePlaceholder
import com.example.myapplication.data.HomeState
import com.example.myapplication.data.Song
import com.example.myapplication.ui.components.AuroraBackground
import com.example.myapplication.ui.components.GlassCard
import com.example.myapplication.ui.components.NowPlayingIndicator
import com.example.myapplication.ui.theme.CardDark
import com.example.myapplication.ui.theme.LightGray
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeState: HomeState,
    onSongClick: (Song, List<Song>) -> Unit,
    onRefresh: () -> Unit,
    currentSongId: String? = null,
    isPlaying: Boolean = false,
    isAuroraTheme: Boolean = false,
    onToggleAurora: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when {
        homeState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading Tamil hits...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LightGray,
                    )
                }
            }
        }
        homeState.error != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = homeState.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = LightGray,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onRefresh) {
                        Text("Retry", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        else -> {
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = homeState.isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = modifier,
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = homeState.isRefreshing,
                        state = pullToRefreshState,
                        containerColor = CardDark,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                if (isAuroraTheme) {
                    AuroraBackground {
                        HomeContent(
                            homeState = homeState,
                            onSongClick = onSongClick,
                            currentSongId = currentSongId,
                            isPlaying = isPlaying,
                            isAuroraTheme = true,
                            onToggleAurora = onToggleAurora,
                        )
                    }
                } else {
                    HomeContent(
                        homeState = homeState,
                        onSongClick = onSongClick,
                        currentSongId = currentSongId,
                        isPlaying = isPlaying,
                        isAuroraTheme = false,
                        onToggleAurora = onToggleAurora,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    homeState: HomeState,
    onSongClick: (Song, List<Song>) -> Unit,
    currentSongId: String?,
    isPlaying: Boolean,
    isAuroraTheme: Boolean = false,
    onToggleAurora: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "D",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AK vibess",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Row {
                IconButton(onClick = onToggleAurora) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Toggle Aurora Theme",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        // Quick-access grid
        if (homeState.quickAccess.isNotEmpty()) {
            QuickAccessGrid(
                songs = homeState.quickAccess,
                onSongClick = { song -> onSongClick(song, homeState.quickAccess) },
                isAuroraTheme = isAuroraTheme,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (homeState.trendingSongs.isNotEmpty()) {
            SongSection(
                title = "Tamil Hits",
                songs = homeState.trendingSongs,
                onSongClick = { song -> onSongClick(song, homeState.trendingSongs) },
                currentSongId = currentSongId,
                isPlaying = isPlaying,
                isAuroraTheme = isAuroraTheme,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (homeState.anirudhHits.isNotEmpty()) {
            SongSection(
                title = "Anirudh Ravichander",
                songs = homeState.anirudhHits,
                onSongClick = { song -> onSongClick(song, homeState.anirudhHits) },
                currentSongId = currentSongId,
                isPlaying = isPlaying,
                isAuroraTheme = isAuroraTheme,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (homeState.arRahmanHits.isNotEmpty()) {
            SongSection(
                title = "A.R. Rahman",
                songs = homeState.arRahmanHits,
                onSongClick = { song -> onSongClick(song, homeState.arRahmanHits) },
                currentSongId = currentSongId,
                isPlaying = isPlaying,
                isAuroraTheme = isAuroraTheme,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (homeState.romanticSongs.isNotEmpty()) {
            SongSection(
                title = "Tamil Love Songs",
                songs = homeState.romanticSongs,
                onSongClick = { song -> onSongClick(song, homeState.romanticSongs) },
                currentSongId = currentSongId,
                isPlaying = isPlaying,
                isAuroraTheme = isAuroraTheme,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (homeState.massSongs.isNotEmpty()) {
            SongSection(
                title = "Mass & Kuthu",
                songs = homeState.massSongs,
                onSongClick = { song -> onSongClick(song, homeState.massSongs) },
                currentSongId = currentSongId,
                isPlaying = isPlaying,
                isAuroraTheme = isAuroraTheme,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (homeState.recentSongs.isNotEmpty()) {
            SongSection(
                title = "New Releases",
                songs = homeState.recentSongs,
                onSongClick = { song -> onSongClick(song, homeState.recentSongs) },
                currentSongId = currentSongId,
                isPlaying = isPlaying,
                isAuroraTheme = isAuroraTheme,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (homeState.devotionalSongs.isNotEmpty()) {
            SongSection(
                title = "Devotional",
                songs = homeState.devotionalSongs,
                onSongClick = { song -> onSongClick(song, homeState.devotionalSongs) },
                currentSongId = currentSongId,
                isPlaying = isPlaying,
                isAuroraTheme = isAuroraTheme,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickAccessGrid(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    isAuroraTheme: Boolean = false,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        val rows = songs.chunked(2)
        rows.forEach { rowSongs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowSongs.forEach { song ->
                    if (isAuroraTheme) {
                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shimmer = false,
                            cornerRadius = 8.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onSongClick(song) },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val placeholder = rememberImagePlaceholder()
                                AsyncImage(
                                    model = song.coverUrl,
                                    contentDescription = song.title,
                                    contentScale = ContentScale.Crop,
                                    placeholder = placeholder,
                                    error = placeholder,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                                )
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .clickable { onSongClick(song) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val placeholder = rememberImagePlaceholder()
                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                placeholder = placeholder,
                                error = placeholder,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                            )
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                    }
                }
                if (rowSongs.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SongSection(
    title: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    currentSongId: String? = null,
    isPlaying: Boolean = false,
    isAuroraTheme: Boolean = false,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(songs) { index, song ->
                val hasAppeared = remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 50L)
                    hasAppeared.value = true
                }
                AnimatedVisibility(
                    visible = hasAppeared.value,
                    enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
                ) {
                    SongCard(
                        song = song,
                        onClick = { onSongClick(song) },
                        isCurrentSong = song.id == currentSongId,
                        isPlaying = isPlaying,
                        isAuroraTheme = isAuroraTheme,
                    )
                }
            }
        }
    }
}

@Composable
private fun SongCard(
    song: Song,
    onClick: () -> Unit,
    isCurrentSong: Boolean = false,
    isPlaying: Boolean = false,
    isAuroraTheme: Boolean = false,
) {
    val cardContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .width(150.dp)
                .clickable(onClick = onClick),
        ) {
            Box {
                val placeholder = rememberImagePlaceholder()
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    placeholder = placeholder,
                    error = placeholder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                )
                if (isCurrentSong) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        NowPlayingIndicator(
                            isPlaying = isPlaying,
                            barWidth = 2.5.dp,
                            maxHeight = 12.dp,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    if (isAuroraTheme) {
        GlassCard {
            cardContent()
        }
    } else {
        cardContent()
    }
}
