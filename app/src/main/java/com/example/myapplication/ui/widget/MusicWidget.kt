package com.example.myapplication.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.myapplication.MainActivity
import com.example.myapplication.R

// Small widget: Play/Pause + Next buttons only (2x1)
class SmallPlayerWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                SmallWidgetContent()
            }
        }
    }
}

@Composable
private fun SmallWidgetContent() {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "DVibess",
            style = TextStyle(
                color = ColorProvider(android.graphics.Color.WHITE),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = "Tap to open",
            style = TextStyle(
                color = ColorProvider(android.graphics.Color.LTGRAY),
                fontSize = 12.sp,
            ),
        )
    }
}

class SmallPlayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = SmallPlayerWidget()
}

// Medium widget: Song info + controls (4x1)
class MediumPlayerWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                MediumWidgetContent()
            }
        }
    }
}

@Composable
private fun MediumWidgetContent() {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "DVibess",
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.WHITE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Tap to play music",
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.LTGRAY),
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

class MediumPlayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MediumPlayerWidget()
}

// Large widget: Full info + controls (4x2)
class LargePlayerWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                LargeWidgetContent()
            }
        }
    }
}

@Composable
private fun LargeWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "DVibess",
            style = TextStyle(
                color = ColorProvider(android.graphics.Color.WHITE),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "Your Tamil music companion",
            style = TextStyle(
                color = ColorProvider(android.graphics.Color.LTGRAY),
                fontSize = 14.sp,
            ),
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Text(
            text = "Tap to open",
            style = TextStyle(
                color = ColorProvider(android.graphics.Color.parseColor("#1DB954")),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

class LargePlayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = LargePlayerWidget()
}
