package com.example.myapplication.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.AuroraAmber
import com.example.myapplication.ui.theme.AuroraCoral
import com.example.myapplication.ui.theme.AuroraMagenta

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "aurora")

    val magentaX by transition.animateFloat(
        initialValue = 0.15f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "magentaX",
    )
    val magentaY by transition.animateFloat(
        initialValue = 0.1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "magentaY",
    )
    val coralX by transition.animateFloat(
        initialValue = 0.6f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "coralX",
    )
    val coralY by transition.animateFloat(
        initialValue = 0.5f, targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "coralY",
    )
    val amberX by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "amberX",
    )
    val amberY by transition.animateFloat(
        initialValue = 0.7f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "amberY",
    )

    val orbRadius = if (Build.VERSION.SDK_INT >= 31) 200f else 300f
    val canvasModifier = if (Build.VERSION.SDK_INT >= 31) {
        Modifier.fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .blur(40.dp)
    } else {
        Modifier.fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    }

    Box(modifier = modifier) {
        Canvas(modifier = canvasModifier) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AuroraMagenta.copy(alpha = 0.4f * intensity), AuroraMagenta.copy(alpha = 0f)),
                    center = Offset(size.width * magentaX, size.height * magentaY),
                    radius = orbRadius,
                ),
                center = Offset(size.width * magentaX, size.height * magentaY),
                radius = orbRadius,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AuroraCoral.copy(alpha = 0.3f * intensity), AuroraCoral.copy(alpha = 0f)),
                    center = Offset(size.width * coralX, size.height * coralY),
                    radius = orbRadius,
                ),
                center = Offset(size.width * coralX, size.height * coralY),
                radius = orbRadius,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AuroraAmber.copy(alpha = 0.2f * intensity), AuroraAmber.copy(alpha = 0f)),
                    center = Offset(size.width * amberX, size.height * amberY),
                    radius = orbRadius,
                ),
                center = Offset(size.width * amberX, size.height * amberY),
                radius = orbRadius,
            )
        }
        content()
    }
}
