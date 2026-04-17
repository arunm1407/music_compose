package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.rememberImagePlaceholder
import com.example.myapplication.data.Song
import com.example.myapplication.ui.components.SongListItem
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumName: String,
    albumArtist: String,
    albumArtUri: String,
    songs: List<Song>,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onShuffle: () -> Unit,
    onSongMore: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                val placeholder = rememberImagePlaceholder()
                AsyncImage(
                    model = albumArtUri,
                    contentDescription = albumName,
                    contentScale = ContentScale.Crop,
                    placeholder = placeholder,
                    error = placeholder,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                startY = 100f,
                            )
                        ),
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(top = 40.dp, start = 8.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                ) {
                    Text(albumName, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(albumArtist, style = MaterialTheme.typography.bodyLarge, color = LightGray)
                    Text("${songs.size} songs", style = MaterialTheme.typography.bodySmall, color = LightGray)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { if (songs.isNotEmpty()) onSongClick(songs.first(), songs) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play")
                }
                OutlinedButton(
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = AccentGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shuffle", color = AccentGreen)
                }
            }
        }

        items(songs) { song ->
            SongListItem(
                song = song,
                onClick = { onSongClick(song, songs) },
                onMoreClick = onSongMore?.let { { it(song) } },
            )
        }
    }
}
