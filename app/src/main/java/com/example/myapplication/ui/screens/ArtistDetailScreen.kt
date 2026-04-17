package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.Song
import com.example.myapplication.ui.components.SongListItem
import com.example.myapplication.ui.components.rememberImagePlaceholder
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.CardDark
import com.example.myapplication.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    artistImageUri: String? = null,
    songs: List<Song>,
    onBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(CardDark),
                    contentAlignment = Alignment.Center,
                ) {
                    if (artistImageUri != null) {
                        AsyncImage(
                            model = artistImageUri,
                            contentDescription = artistName,
                            contentScale = ContentScale.Crop,
                            placeholder = rememberImagePlaceholder(),
                            error = rememberImagePlaceholder(),
                            modifier = Modifier.matchParentSize(),
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = LightGray, modifier = Modifier.size(64.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(artistName, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${songs.size} songs", style = MaterialTheme.typography.bodyMedium, color = LightGray)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.padding(horizontal = 32.dp),
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        items(songs) { song ->
            SongListItem(song = song, onClick = { onSongClick(song, songs) })
        }
    }
}
