package com.example.domain.usecase

import androidx.paging.PagingData
import com.example.data.local.entity.BlacklistEntity
import com.example.domain.model.WallpaperItem
import com.example.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow

class GetFeedUseCase(private val repository: WallpaperRepository) {
    operator fun invoke(
        subreddits: List<String>,
        sort: String,
        nsfwSetting: String,
        blacklist: List<BlacklistEntity>
    ): Flow<PagingData<WallpaperItem>> {
        return repository.getFeed(subreddits, sort, nsfwSetting, blacklist)
    }
}
