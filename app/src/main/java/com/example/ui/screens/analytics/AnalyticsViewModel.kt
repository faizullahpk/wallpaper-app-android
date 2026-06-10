package com.example.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AnalyticsViewModel(
    private val repository: WallpaperRepository
) : ViewModel() {

    val analyticsState: StateFlow<AnalyticsState> = combine(
        repository.getViewedHistory(),
        repository.getFavorites(),
        repository.getDownloads()
    ) { viewed, favorites, downloads ->
        val totalViewed = viewed.size
        val totalFavorites = favorites.size
        val totalDownloads = downloads.size

        // Calculate most viewed subreddit
        val mostViewedSub = viewed
            .groupBy { it.subreddit }
            .maxByOrNull { it.value.size }?.key ?: "None"

        // Calculate favorite categories distribution (Images vs Videos vs GIFs)
        val favCategories = favorites
            .groupBy { it.mediaType.name }
            .mapValues { it.value.size }

        // Assume clean mock disk weight calculation for downloads (e.g. 1.2MB average each)
        val totalStorageMb = totalDownloads * 1.5

        AnalyticsState(
            totalViewed = totalViewed,
            totalFavorites = totalFavorites,
            totalDownloads = totalDownloads,
            storageUsageMb = totalStorageMb,
            mostViewedSubreddit = mostViewedSub,
            favoriteCategoriesDistribution = favCategories
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsState())

    class Factory(
        private val repository: WallpaperRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnalyticsViewModel(repository) as T
        }
    }
}

data class AnalyticsState(
    val totalViewed: Int = 0,
    val totalFavorites: Int = 0,
    val totalDownloads: Int = 0,
    val storageUsageMb: Double = 0.0,
    val mostViewedSubreddit: String = "None",
    val favoriteCategoriesDistribution: Map<String, Int> = emptyMap()
)
