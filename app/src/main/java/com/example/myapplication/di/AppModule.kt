package com.example.myapplication.di

import com.example.myapplication.MusicViewModel
import com.example.myapplication.data.db.AppDatabase
import com.example.myapplication.data.local.LocalMusicScanner
import com.example.myapplication.data.lyrics.LyricsRepository
import com.example.myapplication.data.preferences.AppPreferences
import com.example.myapplication.download.SongDownloadManager
import com.example.myapplication.viewmodel.LibraryViewModel
import com.example.myapplication.viewmodel.LyricsViewModel
import com.example.myapplication.viewmodel.PlaylistViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().favoriteDao() }
    single { get<AppDatabase>().playHistoryDao() }
    single { get<AppDatabase>().playCountDao() }
    single { get<AppDatabase>().lyricsDao() }
    single { get<AppDatabase>().songCacheDao() }
    single { get<AppDatabase>().downloadDao() }

    single { AppPreferences(androidContext()) }
    single { LocalMusicScanner(androidContext()) }
    single { com.example.myapplication.network.LyricsApi() }
    single { LyricsRepository(get(), get()) }
    single { SongDownloadManager(androidContext(), get(), get()) }

    viewModel { MusicViewModel(androidContext()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { PlaylistViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { LyricsViewModel(get(), get()) }
}
