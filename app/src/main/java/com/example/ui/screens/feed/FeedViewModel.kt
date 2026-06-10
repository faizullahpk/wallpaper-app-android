package com.example.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.data.local.entity.BlacklistEntity
import com.example.domain.model.WallpaperItem
import com.example.domain.repository.WallpaperRepository
import com.example.domain.usecase.FavoritesUseCase
import com.example.domain.usecase.GetFeedUseCase
import com.example.domain.usecase.ViewedUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repository: WallpaperRepository,
    private val getFeedUseCase: GetFeedUseCase,
    private val favoritesUseCase: FavoritesUseCase,
    private val viewedUseCase: ViewedUseCase,
    private val settingsManager: com.example.data.preferences.SettingsManager
) : ViewModel() {

    private val _sortOrder = MutableStateFlow("hot") // "hot", "new", "top"
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // NSFW configuration mapping
    val nsfwSetting: StateFlow<String> = settingsManager.nsfwMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BLUR")

    // Blur viewed history setting configuration
    val blurViewed: StateFlow<Boolean> = settingsManager.blurViewed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Load active blacklists
    val blacklist: StateFlow<List<BlacklistEntity>> = repository.getBlacklist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Load enabled subreddits
    val activeSubreddits: StateFlow<List<String>> = repository.getSubreddits()
        .map { list -> list.filter { it.isEnabled }.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val wallpaperFeed: Flow<PagingData<WallpaperItem>> = combine(
        activeSubreddits,
        _sortOrder,
        _searchQuery,
        nsfwSetting,
        blacklist
    ) { subs, sort, query, nsfw, bList ->
        val trimmedQuery = query.removePrefix("r/").trim()
        val subsToQuery = if (trimmedQuery.isNotEmpty() && (subs.any { it.equals(trimmedQuery, ignoreCase = true) } || trimmedQuery.matches(Regex("^[a-zA-Z0-9_]+$")))) {
            listOf(trimmedQuery)
        } else {
            subs
        }
        FeedRequest(subsToQuery, sort, nsfw, bList)
    }.flatMapLatest { req ->
        getFeedUseCase(req.subs, req.sort, req.nsfw, req.blacklist)
    }.cachedIn(viewModelScope)

    fun changeSort(sort: String) {
        _sortOrder.value = sort
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(item: WallpaperItem) {
        viewModelScope.launch {
            if (item.isFavorite) {
                favoritesUseCase.removeFavorite(item.id)
            } else {
                favoritesUseCase.addFavorite(item.copy(isFavorite = true))
            }
        }
    }

    fun markAsViewed(item: WallpaperItem) {
        viewModelScope.launch {
            viewedUseCase.addViewed(item)
        }
    }

    private data class FeedRequest(
        val subs: List<String>,
        val sort: String,
        val nsfw: String,
        val blacklist: List<BlacklistEntity>
    )

    class Factory(
        private val repository: WallpaperRepository,
        private val getFeedUseCase: GetFeedUseCase,
        private val favoritesUseCase: FavoritesUseCase,
        private val viewedUseCase: ViewedUseCase,
        private val settingsManager: com.example.data.preferences.SettingsManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FeedViewModel(
                repository,
                getFeedUseCase,
                favoritesUseCase,
                viewedUseCase,
                settingsManager
            ) as T
        }
    }
}
