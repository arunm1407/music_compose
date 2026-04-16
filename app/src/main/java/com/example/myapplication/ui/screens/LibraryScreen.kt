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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.AuroraBackground
import com.example.myapplication.ui.components.GlassCard
import com.example.myapplication.ui.components.rememberImagePlaceholder
import com.example.myapplication.data.Song
import com.example.myapplication.ui.components.NowPlayingIndicator
import com.example.myapplication.ui.theme.AuroraCoral
import com.example.myapplication.ui.theme.AuroraMagenta
import com.example.myapplication.ui.theme.LightGray
import kotlinx.coroutines.delay

@Composable
fun LibraryScreen(
    songs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit,
    currentSongId: String? = null,
    isPlaying: Boolean = false,
    favoriteSongIds: Set<String> = emptySet(),
    isAuroraTheme: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val content: @Composable () -> Unit = {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White,
                )
            }
        }

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Songs you play will appear here",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LightGray,
                )
            }
        } else {
            val favoriteSongs = songs.filter { it.id in favoriteSongIds }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (favoriteSongs.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = if (isAuroraTheme) listOf(
                                                AuroraMagenta,
                                                AuroraCoral,
                                            ) else listOf(
                                                Color(0xFF450AF5),
                                                Color(0xFFC4EFD9),
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Liked Songs",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                )
                                Text(
                                    text = "${favoriteSongs.size} songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightGray,
                                )
                            }
                        }
                    }
                    itemsIndexed(favoriteSongs) { index, song ->
                        AnimatedLibrarySongItem(
                            index = index,
                            song = song,
                            onClick = { onSongClick(song, favoriteSongs) },
                            isCurrentSong = song.id == currentSongId,
                            isPlaying = isPlaying,
                            isAuroraTheme = isAuroraTheme,
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All Songs",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                    }
                }
                itemsIndexed(songs) { index, song ->
                    AnimatedLibrarySongItem(
                        index = index,
                        song = song,
                        onClick = { onSongClick(song, songs) },
                        isCurrentSong = song.id == currentSongId,
                        isPlaying = isPlaying,
                        isAuroraTheme = isAuroraTheme,
                    )
                }
            }
        }
    }
    }

    if (isAuroraTheme) {
        AuroraBackground { content() }
    } else {
        content()
    }
}

@Composable
private fun AnimatedLibrarySongItem(
    index: Int,
    song: Song,
    onClick: () -> Unit,
    isCurrentSong: Boolean = false,
    isPlaying: Boolean = false,
    isAuroraTheme: Boolean = false,
) {
    var hasAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 50L)
        hasAppeared = true
    }

    AnimatedVisibility(
        visible = hasAppeared,
        enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
    ) {
        if (isAuroraTheme) {
            GlassCard(
                shimmer = false,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                LibrarySongItem(
                    song = song,
                    onClick = onClick,
                    isCurrentSong = isCurrentSong,
                    isPlaying = isPlaying,
                )
            }
        } else {
            LibrarySongItem(
                song = song,
                onClick = onClick,
                isCurrentSong = isCurrentSong,
                isPlaying = isPlaying,
            )
        }
    }
}

@Composable
private fun LibrarySongItem(
    song: Song,
    onClick: () -> Unit,
    isCurrentSong: Boolean = false,
    isPlaying: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
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
                .clip(RoundedCornerShape(4.dp)),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${song.artist} \u2022 Song",
                style = MaterialTheme.typography.bodySmall,
                color = LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isCurrentSong) {
            NowPlayingIndicator(isPlaying = isPlaying)
        }
    }
}
