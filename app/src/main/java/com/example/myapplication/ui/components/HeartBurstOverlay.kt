package com.example.myapplication.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class BurstHeart(
    val emoji: String,
    val fontSize: Int,
    val angle: Double,
    val distance: Float,
    val delayMs: Long,
    val rotation: Float,
    val durationMs: Int,
)

@Composable
fun HeartBurstOverlay(trigger: Int) {
    if (trigger == 0) return

    val hearts = remember(trigger) {
        val emojis = listOf("❤️", "🧡", "💖", "💗", "💕", "💓", "❤️‍🔥", "😍", "🥰", "💘", "💝", "🫶")
        val random = Random(trigger)
        List(24) {
            val angle = random.nextDouble(0.0, 2 * Math.PI)
            BurstHeart(
                emoji = emojis[random.nextInt(emojis.size)],
                fontSize = random.nextInt(48, 120),
                angle = angle,
                distance = 0.2f + random.nextFloat() * 0.8f,
                delayMs = random.nextLong(0, 300),
                rotation = random.nextFloat() * 50f - 25f,
                durationMs = random.nextInt(1200, 2200),
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val halfW = with(LocalDensity.current) { maxWidth.toPx() } / 2f
        val halfH = with(LocalDensity.current) { maxHeight.toPx() } / 2f
        val maxRadius = maxOf(halfW, halfH)

        hearts.forEach { heart ->
            val progress = remember(trigger) { Animatable(0f) }
            val alphaAnim = remember(trigger) { Animatable(0f) }
            val scaleAnim = remember(trigger) { Animatable(0f) }

            LaunchedEffect(trigger) {
                kotlinx.coroutines.delay(heart.delayMs)
                launch {
                    progress.snapTo(0f)
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(heart.durationMs, easing = EaseOutCubic),
                    )
                }
                launch {
                    scaleAnim.snapTo(0f)
                    scaleAnim.animateTo(1.4f, tween(180))
                    scaleAnim.animateTo(1f, tween(200))
                    scaleAnim.animateTo(0.5f, tween(heart.durationMs - 380))
                }
                launch {
                    alphaAnim.snapTo(0f)
                    alphaAnim.animateTo(1f, tween(100))
                    kotlinx.coroutines.delay((heart.durationMs * 0.35f).toLong())
                    alphaAnim.animateTo(0f, tween((heart.durationMs * 0.45f).toInt()))
                }
            }

            val dist = progress.value * heart.distance * maxRadius
            val x = halfW + (cos(heart.angle) * dist).toFloat()
            val y = halfH + (sin(heart.angle) * dist).toFloat()

            Text(
                text = heart.emoji,
                fontSize = heart.fontSize.sp,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = x - halfW
                        translationY = y - halfH
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        rotationZ = heart.rotation * progress.value
                        alpha = alphaAnim.value
                    },
            )
        }
    }
}
