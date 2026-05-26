package com.example.myapplication.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

private class DragSession {
    var active: Boolean = false
    var dragIndex: Int = 0
}

@Composable
fun DragReorderItem(
    itemKey: String,
    index: Int,
    itemCount: Int,
    draggingKey: String?,
    onDragStart: (String) -> Unit,
    onDragEnd: () -> Unit,
    onMove: (Int, Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable (dragHandleModifier: Modifier) -> Unit,
) {
    var dragOffsetY by remember(itemKey) { mutableFloatStateOf(0f) }
    val session = remember(itemKey) { DragSession() }
    val isDragging = draggingKey == itemKey
    val rowHeightPx = with(LocalDensity.current) { 72.dp.toPx() }
    val handleModifier = if (enabled) {
        Modifier.pointerInput(itemKey, itemCount) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    session.active = true
                    session.dragIndex = index
                    dragOffsetY = 0f
                    onDragStart(itemKey)
                },
                onDragEnd = {
                    session.active = false
                    dragOffsetY = 0f
                    onDragEnd()
                },
                onDragCancel = {
                    session.active = false
                    dragOffsetY = 0f
                    onDragEnd()
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    if (!session.active) return@detectDragGesturesAfterLongPress
                    dragOffsetY += dragAmount.y
                    val currentIndex = session.dragIndex
                    when {
                        dragOffsetY > rowHeightPx && currentIndex < itemCount - 1 -> {
                            onMove(currentIndex, currentIndex + 1)
                            session.dragIndex = currentIndex + 1
                            dragOffsetY = 0f
                        }
                        dragOffsetY < -rowHeightPx && currentIndex > 0 -> {
                            onMove(currentIndex, currentIndex - 1)
                            session.dragIndex = currentIndex - 1
                            dragOffsetY = 0f
                        }
                    }
                },
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = if (isDragging || session.active) dragOffsetY else 0f
            },
    ) {
        content(handleModifier)
    }
}

@Composable
fun rememberDragReorderState(): DragReorderState {
    return remember { DragReorderState() }
}

class DragReorderState {
    var draggingKey by mutableStateOf<String?>(null)
        private set

    fun startDrag(key: String) {
        draggingKey = key
    }

    fun endDrag() {
        draggingKey = null
    }
}

fun <T> List<T>.moveItem(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) return this
    return toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}
