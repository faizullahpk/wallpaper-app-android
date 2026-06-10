package com.example.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.MediaType
import com.example.domain.model.WallpaperItem
import com.example.domain.usecase.FavoritesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesUseCase: FavoritesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL") // "ALL", "IMAGES", "VIDEOS", "GIFS"
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _sortBy = MutableStateFlow("DATE") // "DATE", "TITLE", "SCORE"
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    val favorites: StateFlow<List<WallpaperItem>> = combine(
        favoritesUseCase.getFavorites(),
        _searchQuery,
        _selectedCategory,
        _sortBy
    ) { items, query, category, sort ->
        var filteredList = items

        // Search logic
        if (query.isNotBlank()) {
            filteredList = filteredList.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.subreddit.contains(query, ignoreCase = true)
            }
        }

        // Category partitioning
        filteredList = when (category) {
            "IMAGES" -> filteredList.filter { it.mediaType == MediaType.IMAGE }
            "VIDEOS" -> filteredList.filter { it.mediaType == MediaType.VIDEO }
            "GIFS" -> filteredList.filter { it.mediaType == MediaType.GIF }
            else -> filteredList
        }

        // Sorting logic
        when (sort) {
            "TITLE" -> filteredList.sortedBy { it.title }
            "SCORE" -> filteredList.sortedByDescending { it.score }
            else -> filteredList.sortedByDescending { it.createdUtc } // Date
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun changeSort(sort: String) {
        _sortBy.value = sort
    }

    fun removeFavorite(postId: String) {
        viewModelScope.launch {
            favoritesUseCase.removeFavorite(postId)
        }
    }

    class Factory(
        private val favoritesUseCase: FavoritesUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(favoritesUseCase) as T
        }
    }
}
