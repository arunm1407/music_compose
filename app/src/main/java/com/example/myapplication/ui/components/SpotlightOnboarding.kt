package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AccentGreen

data class SpotlightTarget(
    val bounds: Rect,
    val title: String,
    val description: String,
    val tooltipAbove: Boolean = true,
)

@Composable
fun SpotlightOnboarding(
    targets: List<SpotlightTarget>,
    onFinish: () -> Unit,
) {
    if (targets.isEmpty()) return

    var currentStep by remember { mutableIntStateOf(0) }
    val overlayAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        overlayAlpha.animateTo(1f, tween(400))
    }

    val currentTarget = targets[currentStep]
    val isLastStep = currentStep == targets.lastIndex

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseScale",
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                if (isLastStep) {
                    onFinish()
                } else {
                    currentStep++
                }
            },
    ) {
        // Dark overlay with spotlight cutout
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spotlightPadding = 12.dp.toPx()
            val cornerRadius = 16.dp.toPx()
            val bounds = currentTarget.bounds

            // Pulse ring around spotlight
            drawRoundRect(
                color = AccentGreen.copy(alpha = pulseAlpha),
                topLeft = Offset(
                    bounds.left - spotlightPadding - pulseScale,
                    bounds.top - spotlightPadding - pulseScale,
                ),
                size = Size(
                    bounds.width + spotlightPadding * 2 + pulseScale * 2,
                    bounds.height + spotlightPadding * 2 + pulseScale * 2,
                ),
                cornerRadius = CornerRadius(cornerRadius + pulseScale),
            )

            // Overlay with cutout
            val cutoutPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            left = bounds.left - spotlightPadding,
                            top = bounds.top - spotlightPadding,
                            right = bounds.right + spotlightPadding,
                            bottom = bounds.bottom + spotlightPadding,
                        ),
                        cornerRadius = CornerRadius(cornerRadius),
                    )
                )
            }
            clipPath(cutoutPath, clipOp = ClipOp.Difference) {
                drawRect(Color.Black.copy(alpha = 0.82f * overlayAlpha.value))
            }

            // Spotlight border
            drawSpotlightBorder(bounds, spotlightPadding, cornerRadius)
        }

        // Tooltip
        val density = LocalDensity.current
        val tooltipYOffset = with(density) {
            if (currentTarget.tooltipAbove) {
                (currentTarget.bounds.top - 24.dp.toPx()).toInt()
            } else {
                (currentTarget.bounds.bottom + 24.dp.toPx()).toInt()
            }
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300)) + slideInVertically(
                initialOffsetY = { if (currentTarget.tooltipAbove) 40 else -40 },
            ),
            exit = fadeOut(tween(200)) + slideOutVertically(),
            modifier = Modifier.offset { IntOffset(0, tooltipYOffset) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = if (currentTarget.tooltipAbove) Alignment.CenterHorizontally else Alignment.CenterHorizontally,
            ) {
                if (!currentTarget.tooltipAbove) {
                    // Arrow pointing up to target
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(AccentGreen),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tooltip card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1A1A2E))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = currentTarget.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentTarget.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Step indicators + button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Dot indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            targets.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == currentStep) 20.dp else 8.dp, 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (index == currentStep) AccentGreen
                                            else Color.White.copy(alpha = 0.2f)
                                        ),
                                )
                            }
                        }

                        // Next/Done button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AccentGreen)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (isLastStep) "Got it" else "Next",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isLastStep) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                if (currentTarget.tooltipAbove) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(AccentGreen),
                    )
                }
            }
        }

        // Skip button at top
        if (!isLastStep) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 52.dp, end = 20.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onFinish,
                    ),
            )
        }
    }
}

private fun DrawScope.drawSpotlightBorder(
    bounds: Rect,
    padding: Float,
    cornerRadius: Float,
) {
    drawRoundRect(
        color = AccentGreen.copy(alpha = 0.5f),
        topLeft = Offset(bounds.left - padding, bounds.top - padding),
        size = Size(
            bounds.width + padding * 2,
            bounds.height + padding * 2,
        ),
        cornerRadius = CornerRadius(cornerRadius),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
    )
}

fun LayoutCoordinates.toSpotlightTarget(
    title: String,
    description: String,
    tooltipAbove: Boolean = true,
): SpotlightTarget {
    val bounds = boundsInRoot()
    return SpotlightTarget(
        bounds = bounds,
        title = title,
        description = description,
        tooltipAbove = tooltipAbove,
    )
}
