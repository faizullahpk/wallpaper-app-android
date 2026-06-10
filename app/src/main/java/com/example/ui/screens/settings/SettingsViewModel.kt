package com.example.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.SettingsManager
import com.example.service.AutoWallpaperWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val context: Context,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val nsfwMode: StateFlow<String> = settingsManager.nsfwMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BLUR")

    val amoledMode: StateFlow<Boolean> = settingsManager.amoledMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val darkMode: StateFlow<Boolean> = settingsManager.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dynamicColors: StateFlow<Boolean> = settingsManager.dynamicColors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val gridSize: StateFlow<Int> = settingsManager.gridSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val blurViewed: StateFlow<Boolean> = settingsManager.blurViewed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoWallpaperEnabled: StateFlow<Boolean> = settingsManager.autoWallpaperEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoWallpaperSource: StateFlow<String> = settingsManager.autoWallpaperSource
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SUBREDDITS")

    val autoWallpaperIntervalHours: StateFlow<Int> = settingsManager.autoWallpaperIntervalHours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 24)

    fun setNsfwMode(mode: String) {
        viewModelScope.launch { settingsManager.setNsfwMode(mode) }
    }

    fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setAmoledMode(enabled) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setDarkMode(enabled) }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setDynamicColors(enabled) }
    }

    fun setGridSize(size: Int) {
        viewModelScope.launch { settingsManager.setGridSize(size) }
    }

    fun setBlurViewed(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setBlurViewed(enabled) }
    }

    fun setAutoWallpaperEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setAutoWallpaperEnabled(enabled)
            updateWallpaperWorker(enabled, autoWallpaperSource.value, autoWallpaperIntervalHours.value)
        }
    }

    fun setAutoWallpaperSource(source: String) {
        viewModelScope.launch {
            settingsManager.setAutoWallpaperSource(source)
            if (autoWallpaperEnabled.value) {
                updateWallpaperWorker(true, source, autoWallpaperIntervalHours.value)
            }
        }
    }

    fun setAutoWallpaperIntervalHours(hours: Int) {
        viewModelScope.launch {
            settingsManager.setAutoWallpaperIntervalHours(hours)
            if (autoWallpaperEnabled.value) {
                updateWallpaperWorker(true, autoWallpaperSource.value, hours)
            }
        }
    }

    private fun updateWallpaperWorker(enabled: Boolean, source: String, hours: Int) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork("auto_wallpaper_changer_work")
            return
        }

        val request = androidx.work.PeriodicWorkRequestBuilder<AutoWallpaperWorker>(
            hours.toLong(), java.util.concurrent.TimeUnit.HOURS
        )
        .setConstraints(
            androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
        )
        .build()

        workManager.enqueueUniquePeriodicWork(
            "auto_wallpaper_changer_work",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    class Factory(
        private val context: Context,
        private val settingsManager: SettingsManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(context, settingsManager) as T
        }
    }
}
