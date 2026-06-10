package com.example.domain.repository

import androidx.paging.PagingData
import com.example.data.local.entity.*
import com.example.domain.model.WallpaperItem
import kotlinx.coroutines.flow.Flow

interface WallpaperRepository {

    fun getFeed(
        subreddits: List<String>,
        sort: String,
        nsfwSetting: String,
        blacklist: List<BlacklistEntity>
    ): Flow<PagingData<WallpaperItem>>

    fun getFavorites(): Flow<List<WallpaperItem>>
    suspend fun getFavoritesSync(): List<FavoriteEntity>
    suspend fun addFavorite(item: WallpaperItem)
    suspend fun removeFavorite(postId: String)
    fun isFavorite(postId: String): Flow<Boolean>

    fun getViewedHistory(): Flow<List<WallpaperItem>>
    suspend fun addViewed(item: WallpaperItem)
    fun isViewed(postId: String): Flow<Boolean>
    suspend fun clearViewedHistory()

    fun getSubreddits(): Flow<List<SubredditEntity>>
    suspend fun getSubredditsSync(): List<SubredditEntity>
    suspend fun insertSubreddit(subreddit: SubredditEntity)
    suspend fun deleteSubreddit(name: String)
    suspend fun updateSubreddit(subreddit: SubredditEntity)
    suspend fun resetDefaultSubreddits()

    fun getDownloads(): Flow<List<DownloadEntity>>
    suspend fun getDownloadsSync(): List<DownloadEntity>
    suspend fun addDownload(download: DownloadEntity)
    suspend fun updateDownload(downloadId: Long, status: String, progress: Int, filePath: String)
    suspend fun deleteDownload(downloadId: Long)

    fun getBlacklist(): Flow<List<BlacklistEntity>>
    suspend fun addBlacklist(blacklist: BlacklistEntity)
    suspend fun removeBlacklist(id: Int)
}
