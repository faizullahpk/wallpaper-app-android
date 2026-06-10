package com.example.domain.usecase

import com.example.domain.model.WallpaperItem
import com.example.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow

class ViewedUseCase(private val repository: WallpaperRepository) {
    fun getViewedHistory(): Flow<List<WallpaperItem>> = repository.getViewedHistory()

    suspend fun addViewed(item: WallpaperItem) {
        repository.addViewed(item)
    }

    fun isViewed(postId: String): Flow<Boolean> = repository.isViewed(postId)

    suspend fun clearViewedHistory() {
        repository.clearViewedHistory()
    }
}
