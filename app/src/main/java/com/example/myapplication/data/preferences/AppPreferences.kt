package com.example.myapplication.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dvibess_settings")

enum class AppTheme { LIGHT, DARK, AMOLED }
enum class NowPlayingTheme { NORMAL, BLUR, CARD, FULL, GRADIENT, MATERIAL }

class AppPreferences(private val context: Context) {

    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val ACCENT_COLOR = intPreferencesKey("accent_color")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val NOW_PLAYING_THEME = stringPreferencesKey("now_playing_theme")
        val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
        val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
        val MIN_DURATION_FILTER = intPreferencesKey("min_duration_filter")
        val AUTO_FETCH_LYRICS = booleanPreferencesKey("auto_fetch_lyrics")
        val LAST_PLAYED_SONG_ID = stringPreferencesKey("last_played_song_id")
        val LAST_PLAYBACK_POSITION = longPreferencesKey("last_playback_position")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val USE_AURORA_THEME = booleanPreferencesKey("use_aurora_theme")
    }

    val appTheme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        try { AppTheme.valueOf(prefs[Keys.APP_THEME] ?: "DARK") } catch (_: Exception) { AppTheme.DARK }
    }

    val accentColor: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACCENT_COLOR] ?: 0xFF1DB954.toInt()
    }

    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.USE_DYNAMIC_COLOR] ?: false
    }

    val nowPlayingTheme: Flow<NowPlayingTheme> = context.dataStore.data.map { prefs ->
        try { NowPlayingTheme.valueOf(prefs[Keys.NOW_PLAYING_THEME] ?: "NORMAL") } catch (_: Exception) { NowPlayingTheme.NORMAL }
    }

    val crossfadeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.CROSSFADE_ENABLED] ?: false
    }

    val crossfadeDuration: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.CROSSFADE_DURATION] ?: 3000
    }

    val minDurationFilter: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.MIN_DURATION_FILTER] ?: 30
    }

    val autoFetchLyrics: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AUTO_FETCH_LYRICS] ?: true
    }

    val gaplessPlayback: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.GAPLESS_PLAYBACK] ?: true
    }

    val useAuroraTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.USE_AURORA_THEME] ?: true
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.APP_THEME] = theme.name }
    }

    suspend fun setAccentColor(color: Int) {
        context.dataStore.edit { it[Keys.ACCENT_COLOR] = color }
    }

    suspend fun setUseDynamicColor(use: Boolean) {
        context.dataStore.edit { it[Keys.USE_DYNAMIC_COLOR] = use }
    }

    suspend fun setNowPlayingTheme(theme: NowPlayingTheme) {
        context.dataStore.edit { it[Keys.NOW_PLAYING_THEME] = theme.name }
    }

    suspend fun setCrossfadeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CROSSFADE_ENABLED] = enabled }
    }

    suspend fun setCrossfadeDuration(duration: Int) {
        context.dataStore.edit { it[Keys.CROSSFADE_DURATION] = duration }
    }

    suspend fun setMinDurationFilter(seconds: Int) {
        context.dataStore.edit { it[Keys.MIN_DURATION_FILTER] = seconds }
    }

    suspend fun setAutoFetchLyrics(auto: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_FETCH_LYRICS] = auto }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GAPLESS_PLAYBACK] = enabled }
    }

    suspend fun setUseAuroraTheme(use: Boolean) {
        context.dataStore.edit { it[Keys.USE_AURORA_THEME] = use }
    }

    suspend fun setLastPlayedSong(songId: String, position: Long) {
        context.dataStore.edit {
            it[Keys.LAST_PLAYED_SONG_ID] = songId
            it[Keys.LAST_PLAYBACK_POSITION] = position
        }
    }

    val lastPlayedSongId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_PLAYED_SONG_ID]
    }

    val lastPlaybackPosition: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_PLAYBACK_POSITION] ?: 0L
    }
}
