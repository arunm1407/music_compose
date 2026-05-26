package com.example.myapplication.ui.screens.player

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.AuroraBackground
import com.example.myapplication.ui.components.rememberImagePlaceholder
import com.example.myapplication.data.PlayerState
import com.example.myapplication.data.RepeatMode
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.AuroraAmber
import com.example.myapplication.ui.theme.AuroraCoral
import com.example.myapplication.ui.theme.AuroraMagenta
import com.example.myapplication.ui.theme.AuroraVoid
import com.example.myapplication.ui.theme.BackgroundDark
import com.example.myapplication.util.formatDuration

@Composable
fun NormalPlayerLayout(
    playerState: PlayerState,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowLyrics: () -> Unit,
    onShowQueue: () -> Unit,
    isAuroraTheme: Boolean = false,
    isDownloaded: Boolean = false,
    isDownloading: Boolean = false,
    onDownload: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val song = playerState.currentSong ?: return

    val backgroundColor = if (isAuroraTheme) AuroraVoid else BackgroundDark
    val accentColor = if (isAuroraTheme) AuroraMagenta else AccentGreen
    val favoriteActiveColor = if (isAuroraTheme) AuroraMagenta else Color.Red

    val content: @Composable () -> Unit = {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Album art
            val placeholder = rememberImagePlaceholder()
            if (isAuroraTheme) {
                // Aurora glow behind album art
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    // Glow layers
                    val glowModifier = if (Build.VERSION.SDK_INT >= 31) {
                        Modifier.size(316.dp).blur(24.dp)
                    } else {
                        Modifier.size(316.dp)
                    }
                    Box(
                        modifier = glowModifier
                            .offset(x = (-8).dp, y = (-8).dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AuroraMagenta.copy(alpha = 0.5f),
                                        AuroraMagenta.copy(alpha = 0f),
                                    ),
                                ),
                                RoundedCornerShape(16.dp),
                            ),
                    )
                    Box(
                        modifier = glowModifier
                            .offset(x = 8.dp, y = 4.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AuroraCoral.copy(alpha = 0.4f),
                                        AuroraCoral.copy(alpha = 0f),
                                    ),
                                ),
                                RoundedCornerShape(16.dp),
                            ),
                    )
                    Box(
                        modifier = glowModifier
                            .offset(x = 0.dp, y = 10.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AuroraAmber.copy(alpha = 0.35f),
                                        AuroraAmber.copy(alpha = 0f),
                                    ),
                                ),
                                RoundedCornerShape(16.dp),
                            ),
                    )
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        placeholder = placeholder,
                        error = placeholder,
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            } else {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    placeholder = placeholder,
                    error = placeholder,
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Song info
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress slider
            var isSeeking by remember { mutableStateOf(false) }
            var seekPosition by remember { mutableFloatStateOf(0f) }
            val progress = if (playerState.duration > 0) {
                playerState.currentPosition.toFloat() / playerState.duration.toFloat()
            } else 0f
            val displayProgress = if (isSeeking) seekPosition else progress

            Slider(
                value = displayProgress,
                onValueChange = { value ->
                    isSeeking = true
                    seekPosition = value
                },
                onValueChangeFinished = {
                    isSeeking = false
                    onSeek((seekPosition * playerState.duration).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val currentTime = if (isSeeking) {
                    (seekPosition * playerState.duration).toLong()
                } else {
                    playerState.currentPosition
                }
                Text(
                    text = formatDuration(currentTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
                Text(
                    text = formatDuration(playerState.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playerState.shuffleEnabled) accentColor else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(onClick = onSkipPrevious, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }

                // Play/Pause button with aurora breathing glow
                if (isAuroraTheme) {
                    val auroraGradient = Brush.linearGradient(listOf(AuroraMagenta, AuroraCoral))
                    val breathingTransition = rememberInfiniteTransition(label = "breathing")
                    val breathingAlpha by breathingTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.7f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
                        ),
                        label = "breathingAlpha",
                    )
                    val glowAlpha = if (playerState.isPlaying) breathingAlpha else 0.3f

                    Box(contentAlignment = Alignment.Center) {
                        // Glow behind the button
                        val glowMod = if (Build.VERSION.SDK_INT >= 31) {
                            Modifier.size(72.dp).blur(12.dp)
                        } else {
                            Modifier.size(72.dp)
                        }
                        Box(
                            modifier = glowMod
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            AuroraMagenta.copy(alpha = glowAlpha),
                                            AuroraCoral.copy(alpha = glowAlpha),
                                        ),
                                    ),
                                ),
                        )
                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(auroraGradient),
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AccentGreen),
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                IconButton(onClick = onSkipNext, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        imageVector = when (playerState.repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (playerState.repeatMode != RepeatMode.OFF) accentColor else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Unlike" else "Like",
                        tint = if (isFavorite) favoriteActiveColor else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(onClick = onAddToPlaylist) {
                    Icon(
                        Icons.Default.PlaylistAdd,
                        contentDescription = "Add to playlist",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp),
                    )
                }
                when {
                    isDownloading -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = accentColor,
                    )
                    isDownloaded -> Icon(
                        Icons.Default.DownloadDone,
                        contentDescription = "Downloaded",
                        tint = accentColor,
                        modifier = Modifier.size(24.dp),
                    )
                    else -> IconButton(onClick = onDownload) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                IconButton(onClick = onShowLyrics) {
                    Icon(
                        Icons.Default.Lyrics,
                        contentDescription = "Lyrics",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(onClick = onShowQueue) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }

    if (isAuroraTheme) {
        AuroraBackground(intensity = 1.3f) {
            content()
        }
    } else {
        content()
    }
}
