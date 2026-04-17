package com.example.myapplication.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.DVibesLogo
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.BackgroundDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    // --- Animation values ---
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(30f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val burstScale = remember { Animatable(0f) }
    val burstAlpha = remember { Animatable(0f) }
    val fadeOut = remember { Animatable(1f) }

    // Infinite animations for particles and glow
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "particles",
    )
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave",
    )

    // Sequenced animation timeline
    LaunchedEffect(Unit) {
        // 1. Burst ring expands
        burstAlpha.animateTo(0.6f, tween(200))
        burstScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        burstAlpha.animateTo(0f, tween(400))

        // 2. Logo scales in with bounce
        logoAlpha.animateTo(1f, tween(300))
        logoScale.animateTo(1.15f, tween(400, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, tween(200, easing = FastOutSlowInEasing))

        // 3. Title slides up and fades in
        delay(100)
        launch { titleAlpha.animateTo(1f, tween(500)) }
        titleOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing))

        // 4. Subtitle fades in
        delay(200)
        subtitleAlpha.animateTo(1f, tween(400))

        // Hold for a moment
        delay(1200)

        // 5. Fade out everything
        fadeOut.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        onSplashFinished()
    }

    // Particle seeds (stable across recompositions)
    val particles = remember {
        List(18) {
            ParticleSeed(
                angle = Random.nextFloat() * 360f,
                radius = Random.nextFloat() * 0.3f + 0.15f,
                size = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.8f + 0.4f,
                color = listOf(
                    AccentGreen,
                    AccentGreen.copy(alpha = 0.6f),
                    Color(0xFF00E5A0),
                    Color(0xFF00D4FF),
                    Color.White.copy(alpha = 0.5f),
                ).random(),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .alpha(fadeOut.value),
        contentAlignment = Alignment.Center,
    ) {
        // Background glow orbs
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp),
        ) {
            // Top-right green orb
            drawCircle(
                color = AccentGreen.copy(alpha = glowPulse * 0.15f),
                radius = size.minDimension * 0.4f,
                center = Offset(size.width * 0.8f, size.height * 0.2f),
            )
            // Bottom-left teal orb
            drawCircle(
                color = Color(0xFF00D4FF).copy(alpha = glowPulse * 0.1f),
                radius = size.minDimension * 0.35f,
                center = Offset(size.width * 0.15f, size.height * 0.75f),
            )
            // Center subtle purple
            drawCircle(
                color = Color(0xFF6B3FA0).copy(alpha = glowPulse * 0.08f),
                radius = size.minDimension * 0.3f,
                center = Offset(size.width * 0.5f, size.height * 0.5f),
            )
        }

        // Floating particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            particles.forEach { p ->
                val angle = Math.toRadians((p.angle + particlePhase * p.speed).toDouble())
                val r = size.minDimension * p.radius
                val px = cx + (r * cos(angle)).toFloat()
                val py = cy + (r * sin(angle)).toFloat()
                drawCircle(
                    color = p.color.copy(alpha = 0.6f),
                    radius = p.size,
                    center = Offset(px, py),
                )
            }
        }

        // Burst ring
        if (burstAlpha.value > 0f) {
            Canvas(
                modifier = Modifier
                    .size(280.dp)
                    .scale(burstScale.value)
                    .alpha(burstAlpha.value),
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            AccentGreen.copy(alpha = 0.3f),
                            AccentGreen.copy(alpha = 0.6f),
                            Color.Transparent,
                        ),
                        radius = size.minDimension / 2,
                    ),
                )
            }
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Music note icon with glow
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center,
            ) {
                // Glow behind icon
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .blur(30.dp),
                ) {
                    drawCircle(
                        color = AccentGreen.copy(alpha = glowPulse * 0.5f),
                        radius = size.minDimension / 2,
                    )
                }

                // Sound wave bars
                Canvas(modifier = Modifier.size(80.dp)) {
                    val barCount = 5
                    val barWidth = size.width / (barCount * 2.5f)
                    val gap = barWidth * 1.5f
                    val startX = (size.width - (barCount * barWidth + (barCount - 1) * (gap - barWidth))) / 2

                    for (i in 0 until barCount) {
                        val phase = wavePhase + i * 0.8f
                        val heightFraction = (sin(phase.toDouble()) * 0.3f + 0.5f).toFloat()
                        val barHeight = size.height * heightFraction * 0.7f
                        val x = startX + i * gap
                        val y = (size.height - barHeight) / 2

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AccentGreen,
                                    Color(0xFF00E5A0),
                                ),
                                startY = y,
                                endY = y + barHeight,
                            ),
                            topLeft = Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // DVibes logo
            DVibesLogo(
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .scale(0.85f + titleAlpha.value * 0.15f),
                showSignature = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            androidx.compose.material3.Text(
                text = "feel the vibe",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 6.sp,
                    color = Color.White.copy(alpha = 0.5f),
                ),
                modifier = Modifier.alpha(subtitleAlpha.value),
            )
        }
    }
}

private data class ParticleSeed(
    val angle: Float,
    val radius: Float,
    val size: Float,
    val speed: Float,
    val color: Color,
)
