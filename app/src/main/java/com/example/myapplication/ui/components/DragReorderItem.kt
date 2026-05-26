package com.example.myapplication.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun DragReorderItem(
    itemKey: String,
    index: Int,
    itemCount: Int,
    dragState: DragReorderState,
    onDragStart: (String, Int) -> Unit,
    onDragEnd: () -> Unit,
    onMove: (Int, Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable (dragHandleModifier: Modifier) -> Unit,
) {
    val isDragging = dragState.draggingKey == itemKey

    val handleModifier = if (enabled) {
        Modifier.pointerInput(itemKey, enabled) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    onDragStart(itemKey, index)
                },
                onDragEnd = {
                    dragState.endDrag()
                    onDragEnd()
                },
                onDragCancel = {
                    dragState.endDrag()
                    onDragEnd()
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragState.handleDrag(
                        deltaY = dragAmount.y,
                        itemCount = itemCount,
                        onMove = onMove,
                    )
                },
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                if (size.height > 0 && dragState.draggingKey == null) {
                    dragState.updateItemHeight(size.height.toFloat())
                }
            }
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = if (isDragging) dragState.dragOffsetY else 0f
            },
    ) {
        content(handleModifier)
    }
}

@Composable
fun rememberDragReorderState(): DragReorderState {
    val defaultRowHeightPx = with(LocalDensity.current) { 76.dp.toPx() }
    return remember(defaultRowHeightPx) {
        DragReorderState().also { it.setDefaultItemHeight(defaultRowHeightPx) }
    }
}

class DragReorderState {
    var draggingKey by mutableStateOf<String?>(null)
        private set
    var draggingIndex by mutableIntStateOf(-1)
        private set
    var dragOffsetY by mutableFloatStateOf(0f)
        private set
    var itemHeightPx by mutableFloatStateOf(0f)
        private set

    private var defaultHeightPx by mutableFloatStateOf(0f)

    fun setDefaultItemHeight(heightPx: Float) {
        defaultHeightPx = heightPx
        if (itemHeightPx <= 0f) {
            itemHeightPx = heightPx
        }
    }

    fun startDrag(key: String, index: Int) {
        draggingKey = key
        draggingIndex = index
        dragOffsetY = 0f
    }

    fun endDrag() {
        draggingKey = null
        draggingIndex = -1
        dragOffsetY = 0f
    }

    fun updateItemHeight(heightPx: Float) {
        if (heightPx > 0f) {
            itemHeightPx = heightPx
        }
    }

    fun handleDrag(
        deltaY: Float,
        itemCount: Int,
        onMove: (Int, Int) -> Unit,
    ) {
        if (draggingKey == null || draggingIndex !in 0 until itemCount) return

        dragOffsetY += deltaY
        val threshold = itemHeightPx.coerceAtLeast(1f)

        while (dragOffsetY > threshold && draggingIndex < itemCount - 1) {
            onMove(draggingIndex, draggingIndex + 1)
            draggingIndex++
            dragOffsetY -= threshold
        }
        while (dragOffsetY < -threshold && draggingIndex > 0) {
            onMove(draggingIndex, draggingIndex - 1)
            draggingIndex--
            dragOffsetY += threshold
        }
    }
}

fun <T> List<T>.moveItem(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex == toIndex || fromIndex !in indices || toIndex !in indices) return this
    return toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}

fun <T> syncOrderedListByIds(local: List<T>, incoming: List<T>, idSelector: (T) -> String): List<T> {
    if (local.isEmpty()) return incoming
    if (incoming.isEmpty()) return local

    val incomingIds = incoming.map(idSelector)
    val localIds = local.map(idSelector)
    if (incomingIds.toSet() != localIds.toSet()) return incoming

    return localIds.mapNotNull { id -> incoming.find { idSelector(it) == id } }
}
