package com.example.myapplication

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.myapplication.di.appModule
import com.example.myapplication.network.MusicApi
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MusicApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MusicApplication)
            modules(appModule)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(MusicApi.client)
            .crossfade(true)
            .build()
    }
}
