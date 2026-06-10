package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reddit_wallpaper_settings")

class SettingsManager(private val context: Context) {

    companion object {
        val KEY_NSFW_MODE = stringPreferencesKey("nsfw_mode") // "HIDDEN", "BLUR", "VISIBLE"
        val KEY_AMOLED_MODE = booleanPreferencesKey("amoled_mode")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val KEY_GRID_SIZE = intPreferencesKey("grid_size")
        val KEY_BLUR_VIEWED = booleanPreferencesKey("blur_viewed")
        val KEY_AUTO_WALLPAPER_ENABLED = booleanPreferencesKey("auto_wallpaper_enabled")
        val KEY_AUTO_WALLPAPER_SOURCE = stringPreferencesKey("auto_wallpaper_source") // "FAVORITES", "DOWNLOADS", "SUBREDDITS"
        val KEY_AUTO_WALLPAPER_INTERVAL_HOURS = intPreferencesKey("auto_wallpaper_interval_hours") // 1, 12, 24, 168 (1 week)
    }

    val nsfwMode: Flow<String> = context.dataStore.data.map { it[KEY_NSFW_MODE] ?: "BLUR" }
    val amoledMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_AMOLED_MODE] ?: false }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_DARK_MODE] ?: true }
    val dynamicColors: Flow<Boolean> = context.dataStore.data.map { it[KEY_DYNAMIC_COLORS] ?: false }
    val gridSize: Flow<Int> = context.dataStore.data.map { it[KEY_GRID_SIZE] ?: 2 }
    val blurViewed: Flow<Boolean> = context.dataStore.data.map { it[KEY_BLUR_VIEWED] ?: true }
    val autoWallpaperEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_WALLPAPER_ENABLED] ?: false }
    val autoWallpaperSource: Flow<String> = context.dataStore.data.map { it[KEY_AUTO_WALLPAPER_SOURCE] ?: "SUBREDDITS" }
    val autoWallpaperIntervalHours: Flow<Int> = context.dataStore.data.map { it[KEY_AUTO_WALLPAPER_INTERVAL_HOURS] ?: 24 }

    suspend fun setNsfwMode(mode: String) {
        context.dataStore.edit { it[KEY_NSFW_MODE] = mode }
    }

    suspend fun setAmoledMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AMOLED_MODE] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DYNAMIC_COLORS] = enabled }
    }

    suspend fun setGridSize(size: Int) {
        context.dataStore.edit { it[KEY_GRID_SIZE] = size }
    }

    suspend fun setBlurViewed(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BLUR_VIEWED] = enabled }
    }

    suspend fun setAutoWallpaperEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_WALLPAPER_ENABLED] = enabled }
    }

    suspend fun setAutoWallpaperSource(source: String) {
        context.dataStore.edit { it[KEY_AUTO_WALLPAPER_SOURCE] = source }
    }

    suspend fun setAutoWallpaperIntervalHours(hours: Int) {
        context.dataStore.edit { it[KEY_AUTO_WALLPAPER_INTERVAL_HOURS] = hours }
    }
}
