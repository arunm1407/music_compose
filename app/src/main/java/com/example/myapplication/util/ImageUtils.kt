package com.example.myapplication.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

suspend fun extractDominantColor(context: Context, imageUrl: String): Color {
    return try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .size(128, 128)
            .build()
        val result = (loader.execute(request) as? SuccessResult)?.drawable
        val bitmap = (result as? BitmapDrawable)?.bitmap ?: return Color(0xFF1DB954)
        val palette = Palette.from(bitmap).generate()
        val dominantSwatch = palette.dominantSwatch
            ?: palette.vibrantSwatch
            ?: palette.mutedSwatch
        Color(dominantSwatch?.rgb ?: 0xFF1DB954.toInt())
    } catch (_: Exception) {
        Color(0xFF1DB954)
    }
}

suspend fun extractColorPalette(context: Context, imageUrl: String): List<Color> {
    return try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .size(128, 128)
            .build()
        val result = (loader.execute(request) as? SuccessResult)?.drawable
        val bitmap = (result as? BitmapDrawable)?.bitmap ?: return listOf(Color(0xFF1DB954))
        val palette = Palette.from(bitmap).generate()
        listOfNotNull(
            palette.dominantSwatch?.let { Color(it.rgb) },
            palette.vibrantSwatch?.let { Color(it.rgb) },
            palette.mutedSwatch?.let { Color(it.rgb) },
            palette.darkVibrantSwatch?.let { Color(it.rgb) },
            palette.darkMutedSwatch?.let { Color(it.rgb) },
            palette.lightVibrantSwatch?.let { Color(it.rgb) },
        ).ifEmpty { listOf(Color(0xFF1DB954)) }
    } catch (_: Exception) {
        listOf(Color(0xFF1DB954))
    }
}
