package com.example.myapplication.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.AuroraDeepPurple

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shimmer: Boolean = true,
    cornerRadius: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)

    val shimmerOffset = if (shimmer) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val offset by transition.animateFloat(
            initialValue = -1f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
            label = "shimmerOffset",
        )
        offset
    } else {
        0f
    }

    // On API 31+: semi-transparent white creates a frosted glass effect
    // because content behind it shows through the translucent fill.
    // On older APIs: use a solid opaque surface color as fallback (no blur available).
    val bgColor = if (Build.VERSION.SDK_INT >= 31) {
        Color.White.copy(alpha = 0.12f)
    } else {
        AuroraDeepPurple.copy(alpha = 0.85f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor, shape)
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .then(
                if (shimmer) {
                    Modifier.drawWithContent {
                        drawContent()
                        val shimmerBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                            ),
                            start = Offset(size.width * shimmerOffset, 0f),
                            end = Offset(size.width * (shimmerOffset + 0.5f), size.height),
                        )
                        drawRect(shimmerBrush)
                    }
                } else {
                    Modifier
                }
            ),
    ) {
        content()
    }
}
