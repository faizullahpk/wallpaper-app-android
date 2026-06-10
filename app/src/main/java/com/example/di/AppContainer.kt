package com.example.di

import android.content.Context
import com.example.data.local.WallpaperDatabase
import com.example.data.preferences.SettingsManager
import com.example.data.remote.RedditApiService
import com.example.data.repository.WallpaperRepositoryImpl
import com.example.domain.repository.WallpaperRepository
import com.example.domain.usecase.*
import com.example.download.AndroidDownloadManagerOrchestrator
import com.example.wallpaper.WallpaperSetter

class AppContainer(private val context: Context) {

    val apiService: RedditApiService by lazy {
        RedditApiService.create()
    }

    val database: WallpaperDatabase by lazy {
        WallpaperDatabase.getDatabase(context)
    }

    val settingsManager: SettingsManager by lazy {
        SettingsManager(context)
    }

    val repository: WallpaperRepository by lazy {
        WallpaperRepositoryImpl(
            context = context,
            apiService = apiService,
            subredditDao = database.subredditDao(),
            favoriteDao = database.favoriteDao(),
            viewedDao = database.viewedDao(),
            downloadDao = database.downloadDao(),
            blacklistDao = database.blacklistDao()
        )
    }

    val getFeedUseCase: GetFeedUseCase by lazy {
        GetFeedUseCase(repository)
    }

    val favoritesUseCase: FavoritesUseCase by lazy {
        FavoritesUseCase(repository)
    }

    val viewedUseCase: ViewedUseCase by lazy {
        ViewedUseCase(repository)
    }

    val subredditUseCase: SubredditUseCase by lazy {
        SubredditUseCase(repository)
    }

    val downloadManagerOrchestrator: AndroidDownloadManagerOrchestrator by lazy {
        AndroidDownloadManagerOrchestrator(context, repository).apply {
            registerReceiver()
        }
    }

    val wallpaperSetter: WallpaperSetter by lazy {
        WallpaperSetter(context)
    }
}
