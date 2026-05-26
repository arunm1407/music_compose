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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.myapplication.data.Song
import com.example.myapplication.data.db.entity.PlaylistEntity
import com.example.myapplication.download.DownloadProgress
import com.example.myapplication.ui.components.AuroraBackground
import com.example.myapplication.ui.components.CreatePlaylistDialog
import com.example.myapplication.ui.components.GlassCard
import com.example.myapplication.ui.components.NowPlayingIndicator
import com.example.myapplication.ui.components.PlaylistCard
import com.example.myapplication.ui.components.rememberImagePlaceholder
import com.example.myapplication.ui.theme.AuroraCoral
import com.example.myapplication.ui.theme.AuroraMagenta
import com.example.myapplication.ui.theme.LightGray
import kotlinx.coroutines.delay

@Composable
fun LibraryScreen(
    songs: List<Song>,
    playlists: List<PlaylistEntity>,
    playlistSongCounts: Map<Long, Int>,
    favoriteSongs: List<Song>,
    downloadedSongs: List<Song>,
    downloadedSongIds: Set<String>,
    downloadProgress: Map<String, DownloadProgress>,
    onSongClick: (Song, List<Song>) -> Unit,
    onCreatePlaylist: (String, () -> Unit) -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onAddSongToPlaylist: (Song) -> Unit,
    onDownloadsClick: () -> Unit,
    onDownloadSong: (Song) -> Unit,
    currentSongId: String? = null,
    isPlaying: Boolean = false,
    isAuroraTheme: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                onCreatePlaylist(name) { showCreateDialog = false }
            },
        )
    }

    val content: @Composable () -> Unit = {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
        ) {
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
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create playlist", tint = Color.White)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item {
                    SectionTitle("Downloads")
                }
                item {
                    DownloadsRow(
                        count = downloadedSongs.size,
                        isAuroraTheme = isAuroraTheme,
                        onClick = onDownloadsClick,
                    )
                }

                if (playlists.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionTitle("Playlists")
                    }
                    itemsIndexed(playlists) { _, playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            songCount = playlistSongCounts[playlist.id] ?: 0,
                            onClick = { onPlaylistClick(playlist) },
                        )
                    }
                }

                if (favoriteSongs.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionTitle("Liked Songs")
                    }
                    item {
                        LikedSongsRow(
                            count = favoriteSongs.size,
                            isAuroraTheme = isAuroraTheme,
                            onClick = {
                                onSongClick(favoriteSongs.first(), favoriteSongs)
                            },
                        )
                    }
                }

                if (songs.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionTitle("All Songs (${songs.size})")
                    }
                    itemsIndexed(songs) { index, song ->
                        AnimatedLibrarySongItem(
                            index = index,
                            song = song,
                            onClick = { onSongClick(song, songs) },
                            isCurrentSong = song.id == currentSongId,
                            isPlaying = isPlaying,
                            isAuroraTheme = isAuroraTheme,
                            isDownloaded = song.id in downloadedSongIds,
                            isDownloading = downloadProgress[song.id]?.isDownloading == true,
                            downloadProgressValue = downloadProgress[song.id]?.progress ?: 0f,
                            onDownload = { onDownloadSong(song) },
                            onAddToPlaylist = { onAddSongToPlaylist(song) },
                        )
                    }
                } else if (playlists.isEmpty() && downloadedSongs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Songs you play will appear here", color = LightGray)
                        }
                    }
                }
            }
        }
    }

    if (isAuroraTheme) AuroraBackground { content() } else content()
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun DownloadsRow(count: Int, isAuroraTheme: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        if (isAuroraTheme) listOf(AuroraMagenta, AuroraCoral)
                        else listOf(Color(0xFF1DB954), Color(0xFF1ED760)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Downloaded Songs", style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(
                text = when (count) {
                    1 -> "1 song"
                    else -> "$count songs"
                },
                style = MaterialTheme.typography.bodySmall,
                color = LightGray,
            )
        }
    }
}

@Composable
private fun LikedSongsRow(count: Int, isAuroraTheme: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        if (isAuroraTheme) listOf(AuroraMagenta, AuroraCoral)
                        else listOf(Color(0xFF450AF5), Color(0xFFC4EFD9)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Liked Songs", style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(
                text = when (count) {
                    1 -> "1 song"
                    else -> "$count songs"
                },
                style = MaterialTheme.typography.bodySmall,
                color = LightGray,
            )
        }
    }
}

@Composable
private fun AnimatedLibrarySongItem(
    index: Int,
    song: Song,
    onClick: () -> Unit,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    isAuroraTheme: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgressValue: Float,
    onDownload: () -> Unit,
    onAddToPlaylist: () -> Unit,
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
        val item: @Composable () -> Unit = {
            LibrarySongItem(
                song = song,
                onClick = onClick,
                isCurrentSong = isCurrentSong,
                isPlaying = isPlaying,
                isDownloaded = isDownloaded,
                isDownloading = isDownloading,
                downloadProgressValue = downloadProgressValue,
                onDownload = onDownload,
                onAddToPlaylist = onAddToPlaylist,
            )
        }
        if (isAuroraTheme) {
            GlassCard(shimmer = false, modifier = Modifier.padding(vertical = 4.dp)) { item() }
        } else {
            item()
        }
    }
}

@Composable
private fun LibrarySongItem(
    song: Song,
    onClick: () -> Unit,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgressValue: Float,
    onDownload: () -> Unit,
    onAddToPlaylist: () -> Unit,
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
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp)),
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
                text = buildString {
                    append(song.artist)
                    append(" • Song")
                    if (isDownloaded) append(" • Offline")
                },
                style = MaterialTheme.typography.bodySmall,
                color = LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isCurrentSong) {
            NowPlayingIndicator(isPlaying = isPlaying)
            Spacer(modifier = Modifier.width(8.dp))
        }
        IconButton(onClick = onAddToPlaylist, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.PlaylistAdd,
                contentDescription = "Add to playlist",
                tint = LightGray,
                modifier = Modifier.size(20.dp),
            )
        }
        when {
            isDownloading -> CircularProgressIndicator(
                progress = { downloadProgressValue },
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
            isDownloaded -> Icon(
                Icons.Default.DownloadDone,
                contentDescription = "Downloaded",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            else -> IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Download, contentDescription = "Download", tint = LightGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun DownloadsScreen(
    songs: List<Song>,
    currentSongId: String?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onDeleteDownload: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(top = 48.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Downloads", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        }
        if (songs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No downloaded songs yet", color = LightGray)
                    Text("Tap download on any song to save offline", color = LightGray.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                itemsIndexed(songs) { _, song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSongClick(song, songs) }
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
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp)),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1)
                            Text("${song.artist} • Offline", style = MaterialTheme.typography.bodySmall, color = LightGray, maxLines = 1)
                        }
                        IconButton(onClick = { onDeleteDownload(song.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove download",
                                tint = LightGray,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
