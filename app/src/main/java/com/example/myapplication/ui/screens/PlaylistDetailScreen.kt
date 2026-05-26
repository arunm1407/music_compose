package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.Song
import com.example.myapplication.download.DownloadProgress
import com.example.myapplication.ui.components.DragReorderItem
import com.example.myapplication.ui.components.moveItem
import com.example.myapplication.ui.components.rememberDragReorderState
import com.example.myapplication.ui.components.rememberImagePlaceholder
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.CardDark
import com.example.myapplication.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    songs: List<Song>,
    downloadedSongIds: Set<String> = emptySet(),
    downloadProgress: Map<String, DownloadProgress> = emptyMap(),
    onDownloadSong: ((Song) -> Unit)? = null,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onShuffle: () -> Unit,
    onRename: ((String) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onAddSongs: (() -> Unit)? = null,
    onRemoveSong: ((Song) -> Unit)? = null,
    onReorderSongs: ((List<Song>) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(playlistName) }
    var orderedSongs by remember(songs) { mutableStateOf(songs) }
    var isReordering by remember { mutableStateOf(false) }
    LaunchedEffect(songs) {
        if (!isReordering) orderedSongs = songs
    }
    val dragState = rememberDragReorderState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        item {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (onAddSongs != null) {
                        IconButton(onClick = onAddSongs) {
                            Icon(Icons.Default.Add, contentDescription = "Add songs", tint = AccentGreen)
                        }
                    }
                    if (onRename != null) {
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color.White)
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete playlist", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(playlistName, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${orderedSongs.size} songs", style = MaterialTheme.typography.bodyMedium, color = LightGray)
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { if (orderedSongs.isNotEmpty()) onSongClick(orderedSongs.first(), orderedSongs) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play")
                    }
                    OutlinedButton(onClick = onShuffle) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = AccentGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shuffle", color = AccentGreen)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (orderedSongs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("No songs in this playlist", color = LightGray)
                    if (onAddSongs != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddSongs,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add songs")
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Hold and drag to reorder",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentGreen,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            itemsIndexed(orderedSongs, key = { _, song -> song.id }) { index, song ->
                val isDragging = dragState.draggingKey == song.id
                DragReorderItem(
                    itemKey = song.id,
                    index = index,
                    itemCount = orderedSongs.size,
                    draggingKey = dragState.draggingKey,
                    onDragStart = {
                        isReordering = true
                        dragState.startDrag(it)
                    },
                    onDragEnd = {
                        dragState.endDrag()
                        isReordering = false
                        onReorderSongs?.invoke(orderedSongs)
                    },
                    onMove = { from, to ->
                        orderedSongs = orderedSongs.moveItem(from, to)
                    },
                    enabled = onReorderSongs != null,
                ) { handleModifier ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDragging) AccentGreen.copy(alpha = 0.18f) else Color.Transparent)
                            .clickable { onSongClick(song, orderedSongs) }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Drag to reorder",
                            tint = LightGray,
                            modifier = handleModifier
                                .size(36.dp)
                                .padding(6.dp),
                        )
                        val placeholder = rememberImagePlaceholder()
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            placeholder = placeholder,
                            error = placeholder,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(6.dp)),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${song.artist} • ${song.album}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (onDownloadSong != null) {
                            when {
                                downloadProgress[song.id]?.isDownloading == true -> CircularProgressIndicator(
                                    progress = { downloadProgress[song.id]?.progress ?: 0f },
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                )
                                song.id in downloadedSongIds -> Icon(
                                    Icons.Default.DownloadDone,
                                    contentDescription = "Downloaded",
                                    tint = AccentGreen,
                                    modifier = Modifier.size(20.dp),
                                )
                                else -> IconButton(onClick = { onDownloadSong(song) }, modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = LightGray,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        } else if (song.id in downloadedSongIds) {
                            Icon(
                                Icons.Default.DownloadDone,
                                contentDescription = "Downloaded",
                                tint = AccentGreen,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp),
                            )
                        }
                        if (onRemoveSong != null) {
                            IconButton(onClick = { onRemoveSong(song) }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove from playlist",
                                    tint = LightGray,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = CardDark,
            titleContentColor = Color.White,
            title = { Text("Rename Playlist") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AccentGreen,
                        focusedBorderColor = AccentGreen,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) {
                        onRename?.invoke(renameText.trim())
                        showRenameDialog = false
                    }
                }) { Text("Rename", color = AccentGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel", color = Color.White) }
            },
        )
    }
}
