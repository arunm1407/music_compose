package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.CardDark
import com.example.myapplication.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerDialog(
    isTimerActive: Boolean,
    remainingMs: Long,
    onDismiss: () -> Unit,
    onSetTimer: (Long) -> Unit,
    onCancelTimer: () -> Unit,
) {
    val presets = listOf(
        "15 min" to 15L * 60 * 1000,
        "30 min" to 30L * 60 * 1000,
        "45 min" to 45L * 60 * 1000,
        "1 hour" to 60L * 60 * 1000,
        "2 hours" to 120L * 60 * 1000,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sleep Timer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isTimerActive) {
                val minutes = remainingMs / 60000
                val seconds = (remainingMs % 60000) / 1000
                Text(
                    text = "Stopping in ${minutes}m ${seconds}s",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentGreen,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCancelTimer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.TimerOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Timer")
                }
            } else {
                Text(
                    text = "Stop playing after",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightGray,
                )
                Spacer(modifier = Modifier.height(16.dp))
                presets.forEach { (label, durationMs) ->
                    OutlinedButton(
                        onClick = { onSetTimer(durationMs) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    ) {
                        Text(label)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
