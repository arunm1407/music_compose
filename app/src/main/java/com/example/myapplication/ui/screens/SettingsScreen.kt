package com.example.myapplication.ui.screens

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.preferences.AppTheme
import com.example.myapplication.data.preferences.NowPlayingTheme
import com.example.myapplication.ui.components.AccentColorPicker
import com.example.myapplication.ui.theme.AccentGreen
import com.example.myapplication.ui.theme.CardDark
import com.example.myapplication.ui.theme.LightGray
import com.example.myapplication.viewmodel.SettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsState: SettingsState,
    onBack: (() -> Unit)? = null,
    onSetTheme: (AppTheme) -> Unit,
    onSetAccentColor: (Int) -> Unit,
    onSetDynamicColor: (Boolean) -> Unit,
    onSetNowPlayingTheme: (NowPlayingTheme) -> Unit,
    onSetCrossfade: (Boolean) -> Unit,
    onSetCrossfadeDuration: (Int) -> Unit,
    onSetMinDuration: (Int) -> Unit,
    onSetAutoLyrics: (Boolean) -> Unit,
    onSetGapless: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showNpThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
    ) {
        // Header
        if (onBack != null) {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        } else {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(16.dp),
            )
        }

        // Appearance
        SectionHeader("Appearance")
        SettingsItem(
            icon = Icons.Default.Palette,
            title = "App Theme",
            subtitle = settingsState.appTheme.name.lowercase().replaceFirstChar { it.uppercase() },
            onClick = { showThemeDialog = true },
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsSwitch(
                icon = Icons.Default.AutoAwesome,
                title = "Dynamic Colors",
                subtitle = "Use Material You colors",
                checked = settingsState.useDynamicColor,
                onCheckedChange = onSetDynamicColor,
            )
        }
        SettingsItem(
            icon = Icons.Default.ColorLens,
            title = "Accent Color",
            subtitle = "Customize app accent color",
            onClick = { showColorPicker = true },
        )
        SettingsItem(
            icon = Icons.Default.MusicNote,
            title = "Now Playing Theme",
            subtitle = settingsState.nowPlayingTheme.name.lowercase().replaceFirstChar { it.uppercase() },
            onClick = { showNpThemeDialog = true },
        )

        // Audio
        SectionHeader("Audio")
        SettingsSwitch(
            icon = Icons.Default.SwapHoriz,
            title = "Crossfade",
            subtitle = "Smooth transition between songs",
            checked = settingsState.crossfadeEnabled,
            onCheckedChange = onSetCrossfade,
        )
        if (settingsState.crossfadeEnabled) {
            Column(modifier = Modifier.padding(horizontal = 56.dp)) {
                Text("Duration: ${settingsState.crossfadeDuration / 1000}s", color = LightGray, style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = settingsState.crossfadeDuration.toFloat(),
                    onValueChange = { onSetCrossfadeDuration(it.toInt()) },
                    valueRange = 1000f..10000f,
                    colors = SliderDefaults.colors(thumbColor = AccentGreen, activeTrackColor = AccentGreen),
                )
            }
        }
        SettingsSwitch(
            icon = Icons.Default.Speed,
            title = "Gapless Playback",
            subtitle = "No silence between tracks",
            checked = settingsState.gaplessPlayback,
            onCheckedChange = onSetGapless,
        )

        // Library
        SectionHeader("Library")
        SettingsItem(
            icon = Icons.Default.FilterList,
            title = "Minimum Duration",
            subtitle = "Skip files shorter than ${settingsState.minDurationFilter}s",
            onClick = {
                val next = when (settingsState.minDurationFilter) {
                    0 -> 10; 10 -> 30; 30 -> 60; else -> 0
                }
                onSetMinDuration(next)
            },
        )

        // Lyrics
        SectionHeader("Lyrics")
        SettingsSwitch(
            icon = Icons.Default.Lyrics,
            title = "Auto-fetch Lyrics",
            subtitle = "Automatically download lyrics for playing songs",
            checked = settingsState.autoFetchLyrics,
            onCheckedChange = onSetAutoLyrics,
        )

        // Data
        SectionHeader("Data")
        SettingsItem(
            icon = Icons.Default.DeleteSweep,
            title = "Clear Play History",
            subtitle = "Remove all recently played data",
            onClick = onClearHistory,
        )

        // About
        SectionHeader("About")
        SettingsItem(
            icon = Icons.Default.Info,
            title = "DVibess",
            subtitle = "Version 1.0",
            onClick = { },
        )
    }

    // Dialogs
    if (showColorPicker) {
        AccentColorPicker(
            selectedColor = settingsState.accentColor,
            onColorSelected = onSetAccentColor,
            onDismiss = { showColorPicker = false },
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = CardDark,
            titleContentColor = Color.White,
            title = { Text("App Theme") },
            text = {
                Column {
                    AppTheme.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetTheme(theme); showThemeDialog = false }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settingsState.appTheme == theme,
                                onClick = { onSetTheme(theme); showThemeDialog = false },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentGreen),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = Color.White,
                            )
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }

    if (showNpThemeDialog) {
        AlertDialog(
            onDismissRequest = { showNpThemeDialog = false },
            containerColor = CardDark,
            titleContentColor = Color.White,
            title = { Text("Now Playing Theme") },
            text = {
                Column {
                    NowPlayingTheme.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetNowPlayingTheme(theme); showNpThemeDialog = false }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settingsState.nowPlayingTheme == theme,
                                onClick = { onSetNowPlayingTheme(theme); showNpThemeDialog = false },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentGreen),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = Color.White,
                            )
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = AccentGreen,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LightGray)
        }
    }
}

@Composable
private fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LightGray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentGreen,
                uncheckedThumbColor = LightGray,
                uncheckedTrackColor = Color.DarkGray,
            ),
        )
    }
}
