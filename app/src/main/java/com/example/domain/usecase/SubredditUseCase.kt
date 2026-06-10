package com.example.domain.usecase

import com.example.data.local.entity.SubredditEntity
import com.example.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow

class SubredditUseCase(private val repository: WallpaperRepository) {
    fun getSubreddits(): Flow<List<SubredditEntity>> = repository.getSubreddits()

    suspend fun addSubreddit(name: String, isCustom: Boolean = true) {
        val trimmed = name.trim().replace("r/", "", ignoreCase = true)
        if (trimmed.isNotBlank()) {
            val count = repository.getSubredditsSync().size
            repository.insertSubreddit(
                SubredditEntity(
                    name = trimmed,
                    orderIndex = count,
                    isEnabled = true,
                    isCustom = isCustom
                )
            )
        }
    }

    suspend fun removeSubreddit(name: String) {
        repository.deleteSubreddit(name)
    }

    suspend fun toggleSubreddit(subreddit: SubredditEntity) {
        repository.updateSubreddit(subreddit.copy(isEnabled = !subreddit.isEnabled))
    }

    suspend fun updateSubreddit(subreddit: SubredditEntity) {
        repository.updateSubreddit(subreddit)
    }

    suspend fun resetDefaults() {
        repository.resetDefaultSubreddits()
    }

    suspend fun reorderSubreddits(list: List<SubredditEntity>) {
        list.forEachIndexed { index, sub ->
            repository.updateSubreddit(sub.copy(orderIndex = index))
        }
    }
}
