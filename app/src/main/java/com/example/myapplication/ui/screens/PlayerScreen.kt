package com.example.myapplication.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.myapplication.data.PlayerState
import com.example.myapplication.data.RepeatMode
import com.example.myapplication.data.preferences.NowPlayingTheme
import com.example.myapplication.ui.screens.player.BlurPlayerLayout
import com.example.myapplication.ui.screens.player.CardPlayerLayout
import com.example.myapplication.ui.screens.player.FullPlayerLayout
import com.example.myapplication.ui.screens.player.GradientPlayerLayout
import com.example.myapplication.ui.screens.player.MaterialPlayerLayout
import com.example.myapplication.ui.screens.player.NormalPlayerLayout
import com.example.myapplication.ui.theme.AccentGreen
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
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
    nowPlayingTheme: NowPlayingTheme = NowPlayingTheme.NORMAL,
    onShowLyrics: () -> Unit = {},
    onShowQueue: () -> Unit = {},
    onShowSleepTimer: () -> Unit = {},
    isAuroraTheme: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val song = playerState.currentSong ?: return

    val swipeOffsetY = remember { Animatable(0f) }
    val swipeThreshold = 300f

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, swipeOffsetY.value.toInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (swipeOffsetY.value > swipeThreshold) {
                            onBackClick()
                        } else {
                            kotlinx.coroutines.MainScope().launch {
                                swipeOffsetY.animateTo(0f, tween(200))
                            }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (swipeOffsetY.value + dragAmount).coerceAtLeast(0f)
                        kotlinx.coroutines.MainScope().launch {
                            swipeOffsetY.snapTo(newOffset)
                        }
                    },
                )
            },
    ) {
        when (nowPlayingTheme) {
            NowPlayingTheme.NORMAL -> NormalPlayerLayout(
                playerState = playerState,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onToggleFavorite = onToggleFavorite,
                onShowLyrics = onShowLyrics,
                onShowQueue = onShowQueue,
                isAuroraTheme = isAuroraTheme,
            )
            NowPlayingTheme.BLUR -> BlurPlayerLayout(
                playerState = playerState,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onToggleFavorite = onToggleFavorite,
                onShowLyrics = onShowLyrics,
                onShowQueue = onShowQueue,
            )
            NowPlayingTheme.CARD -> CardPlayerLayout(
                playerState = playerState,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onToggleFavorite = onToggleFavorite,
                onShowLyrics = onShowLyrics,
                onShowQueue = onShowQueue,
            )
            NowPlayingTheme.FULL -> FullPlayerLayout(
                playerState = playerState,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onToggleFavorite = onToggleFavorite,
                onShowLyrics = onShowLyrics,
                onShowQueue = onShowQueue,
            )
            NowPlayingTheme.GRADIENT -> GradientPlayerLayout(
                playerState = playerState,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onToggleFavorite = onToggleFavorite,
                onShowLyrics = onShowLyrics,
                onShowQueue = onShowQueue,
            )
            NowPlayingTheme.MATERIAL -> MaterialPlayerLayout(
                playerState = playerState,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onSeek = onSeek,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onToggleFavorite = onToggleFavorite,
                onShowLyrics = onShowLyrics,
                onShowQueue = onShowQueue,
            )
        }

    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
