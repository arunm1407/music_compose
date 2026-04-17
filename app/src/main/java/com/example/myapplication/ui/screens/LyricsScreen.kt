package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.LyricLine
import com.example.myapplication.data.LyricsState
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.LightGray

@Composable
fun LyricsScreen(
    lyricsState: LyricsState,
    songTitle: String,
    songArtist: String,
    onDismiss: () -> Unit,
    onSeekToLine: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Auto-scroll to current line
    LaunchedEffect(lyricsState.currentLineIndex) {
        if (lyricsState.currentLineIndex >= 0 && lyricsState.hasSyncedLyrics) {
            listState.animateScrollToItem(
                index = lyricsState.currentLineIndex,
                scrollOffset = -200,
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .padding(top = 48.dp),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = songTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = songArtist,
                    style = MaterialTheme.typography.bodySmall,
                    color = LightGray,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            lyricsState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            }
            lyricsState.hasSyncedLyrics -> {
                // Synced lyrics
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(lyricsState.syncedLyrics) { index, line ->
                        val isActive = index == lyricsState.currentLineIndex
                        Text(
                            text = line.text,
                            fontSize = if (isActive) 24.sp else 18.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) AccentGreen else Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSeekToLine?.invoke(line.timestampMs) },
                        )
                    }
                }
            }
            lyricsState.lyrics.isNotBlank() -> {
                // Plain lyrics
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    item {
                        Text(
                            text = lyricsState.lyrics,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 28.sp,
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No lyrics available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LightGray,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lyrics couldn't be found for this song",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightGray.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}
