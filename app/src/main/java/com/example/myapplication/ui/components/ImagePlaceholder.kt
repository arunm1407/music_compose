package com.example.myapplication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import com.example.myapplication.ui.theme.CardDark

@Composable
fun rememberImagePlaceholder(): ColorPainter {
    return remember { ColorPainter(CardDark) }
}
