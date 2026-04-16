package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.myapplication.ui.components.AuroraBackground
import com.example.myapplication.ui.components.GlassCard
import com.example.myapplication.ui.components.NowPlayingIndicator
import com.example.myapplication.ui.components.rememberImagePlaceholder
import com.example.myapplication.ui.theme.AuroraAmber
import com.example.myapplication.ui.theme.AuroraCoral
import com.example.myapplication.ui.theme.AuroraDeepPurple
import com.example.myapplication.ui.theme.AuroraMagenta
import com.example.myapplication.ui.theme.AuroraUltraviolet
import com.example.myapplication.ui.theme.LightGray
import kotlinx.coroutines.delay

private val genreColors = listOf(
    Color(0xFFE13300),
    Color(0xFF1E3264),
    Color(0xFF8400E7),
    Color(0xFF158A08),
    Color(0xFFE8115B),
    Color(0xFF056952),
    Color(0xFFE91429),
    Color(0xFF509BF5),
)

private val auroraGenreGradients = listOf(
    listOf(AuroraMagenta, AuroraCoral),
    listOf(AuroraCoral, AuroraAmber),
    listOf(AuroraUltraviolet, AuroraMagenta),
    listOf(AuroraMagenta, AuroraAmber),
    listOf(AuroraCoral, AuroraMagenta),
    listOf(AuroraAmber, AuroraCoral),
    listOf(AuroraDeepPurple, AuroraMagenta),
    listOf(AuroraUltraviolet, AuroraCoral),
)

private val genres = listOf(
    "Tamil Hits", "Anirudh", "AR Rahman", "Yuvan",
    "Ilaiyaraaja", "Hip-Hop Tamil", "Tamil Melody", "Kuthu"
)

@Composable
fun SearchScreen(
    searchResults: List<Song>,
    isSearching: Boolean,
    onSearch: (String) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    currentSongId: String? = null,
    isPlaying: Boolean = false,
    isAuroraTheme: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val accentColor = MaterialTheme.colorScheme.primary

    // Debounce search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            onSearch("")
            return@LaunchedEffect
        }
        delay(500)
        onSearch(searchQuery)
    }

    val content: @Composable () -> Unit = {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Songs, artists, albums...",
                    color = if (isAuroraTheme) Color.White.copy(alpha = 0.4f) else LightGray,
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = if (isAuroraTheme) Color.White.copy(alpha = 0.7f) else Color.Black,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = if (isAuroraTheme) {
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.12f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AuroraMagenta,
                    focusedBorderColor = Color.White.copy(alpha = 0.18f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                )
            } else {
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isNotBlank()) {
            when {
                isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = accentColor, modifier = Modifier.size(32.dp))
                    }
                }
                searchResults.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No results found for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LightGray,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(searchResults) { song ->
                            if (isAuroraTheme) {
                                GlassCard(shimmer = false) {
                                    SearchResultItem(
                                        song = song,
                                        onClick = { onSongClick(song, searchResults) },
                                        isCurrentSong = song.id == currentSongId,
                                        isPlaying = isPlaying,
                                        isAuroraTheme = true,
                                    )
                                }
                            } else {
                                SearchResultItem(
                                    song = song,
                                    onClick = { onSongClick(song, searchResults) },
                                    isCurrentSong = song.id == currentSongId,
                                    isPlaying = isPlaying,
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Genre grid for quick Tamil searches
            Text(
                text = "Browse Tamil",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(genres.size) { index ->
                    val chipModifier = if (isAuroraTheme) {
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(auroraGenreGradients[index % auroraGenreGradients.size]),
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { searchQuery = genres[index] }
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(genreColors[index % genreColors.size])
                            .clickable { searchQuery = genres[index] }
                    }
                    Box(
                        modifier = chipModifier,
                        contentAlignment = Alignment.TopStart,
                    ) {
                        Text(
                            text = genres[index],
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
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
private fun SearchResultItem(
    song: Song,
    onClick: () -> Unit,
    isCurrentSong: Boolean = false,
    isPlaying: Boolean = false,
    isAuroraTheme: Boolean = false,
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
                .size(48.dp)
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
                text = "${song.artist} \u2022 ${song.album}",
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
