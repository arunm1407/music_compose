package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
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
import com.example.myapplication.ui.components.rememberImagePlaceholder
import com.example.myapplication.data.Song
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.CardDark
import com.example.myapplication.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    queue: List<Song>,
    currentIndex: Int,
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClearQueue: () -> Unit,
    onSaveAsPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Top bar
        TopAppBar(
            title = { Text("Playing Queue", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = onSaveAsPlaylist) {
                    Icon(Icons.Default.Save, contentDescription = "Save as playlist", tint = AccentGreen)
                }
                IconButton(onClick = onClearQueue) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear queue", tint = Color.Red.copy(alpha = 0.7f))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
            ),
        )

        if (queue.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Queue is empty", color = LightGray, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Now playing header
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.labelMedium,
                color = AccentGreen,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                itemsIndexed(queue) { index, song ->
                    val isCurrentlyPlaying = index == currentIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSongClick(index) }
                            .background(if (isCurrentlyPlaying) AccentGreen.copy(alpha = 0.1f) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isCurrentlyPlaying) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Playing",
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        val placeholder = rememberImagePlaceholder()
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            placeholder = placeholder,
                            error = placeholder,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp)),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isCurrentlyPlaying) AccentGreen else Color.White,
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
                        IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = LightGray, modifier = Modifier.size(18.dp))
                        }
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Reorder",
                            tint = LightGray,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
