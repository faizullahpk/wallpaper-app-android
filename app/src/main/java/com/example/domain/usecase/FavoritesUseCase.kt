package com.example.domain.usecase

import com.example.domain.model.WallpaperItem
import com.example.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow

class FavoritesUseCase(private val repository: WallpaperRepository) {
    fun getFavorites(): Flow<List<WallpaperItem>> = repository.getFavorites()

    suspend fun addFavorite(item: WallpaperItem) {
        repository.addFavorite(item)
    }

    suspend fun removeFavorite(postId: String) {
        repository.removeFavorite(postId)
    }

    fun isFavorite(postId: String): Flow<Boolean> = repository.isFavorite(postId)
}
