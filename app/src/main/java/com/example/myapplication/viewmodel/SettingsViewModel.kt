package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.preferences.AppPreferences
import com.example.myapplication.data.preferences.AppTheme
import com.example.myapplication.data.preferences.NowPlayingTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsState(
    val appTheme: AppTheme = AppTheme.DARK,
    val accentColor: Int = 0xFF1DB954.toInt(),
    val useDynamicColor: Boolean = false,
    val nowPlayingTheme: NowPlayingTheme = NowPlayingTheme.NORMAL,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3000,
    val minDurationFilter: Int = 30,
    val autoFetchLyrics: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val useAuroraTheme: Boolean = true,
)

class SettingsViewModel(
    private val preferences: AppPreferences,
) : ViewModel() {

    val settingsState: StateFlow<SettingsState> = combine(
        preferences.appTheme,
        preferences.accentColor,
        preferences.useDynamicColor,
        preferences.nowPlayingTheme,
        preferences.crossfadeEnabled,
    ) { theme, accent, dynamic, npTheme, crossfade ->
        SettingsState(
            appTheme = theme,
            accentColor = accent,
            useDynamicColor = dynamic,
            nowPlayingTheme = npTheme,
            crossfadeEnabled = crossfade,
        )
    }.combine(
        combine(
            preferences.crossfadeDuration,
            preferences.minDurationFilter,
            preferences.autoFetchLyrics,
            preferences.gaplessPlayback,
        ) { duration, minFilter, autoLyrics, gapless ->
            listOf(duration, minFilter, if (autoLyrics) 1 else 0, if (gapless) 1 else 0)
        }
    ) { state, extra ->
        state.copy(
            crossfadeDuration = extra[0],
            minDurationFilter = extra[1],
            autoFetchLyrics = extra[2] == 1,
            gaplessPlayback = extra[3] == 1,
        )
    }.combine(preferences.useAuroraTheme) { state, aurora ->
        state.copy(useAuroraTheme = aurora)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsState())

    fun setAppTheme(theme: AppTheme) { viewModelScope.launch { preferences.setAppTheme(theme) } }
    fun setAccentColor(color: Int) { viewModelScope.launch { preferences.setAccentColor(color) } }
    fun setUseDynamicColor(use: Boolean) { viewModelScope.launch { preferences.setUseDynamicColor(use) } }
    fun setNowPlayingTheme(theme: NowPlayingTheme) { viewModelScope.launch { preferences.setNowPlayingTheme(theme) } }
    fun setCrossfadeEnabled(enabled: Boolean) { viewModelScope.launch { preferences.setCrossfadeEnabled(enabled) } }
    fun setCrossfadeDuration(duration: Int) { viewModelScope.launch { preferences.setCrossfadeDuration(duration) } }
    fun setMinDurationFilter(seconds: Int) { viewModelScope.launch { preferences.setMinDurationFilter(seconds) } }
    fun setAutoFetchLyrics(auto: Boolean) { viewModelScope.launch { preferences.setAutoFetchLyrics(auto) } }
    fun setGaplessPlayback(enabled: Boolean) { viewModelScope.launch { preferences.setGaplessPlayback(enabled) } }
    fun setUseAuroraTheme(use: Boolean) { viewModelScope.launch { preferences.setUseAuroraTheme(use) } }
}
