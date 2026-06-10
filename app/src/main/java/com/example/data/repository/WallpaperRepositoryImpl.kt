package com.example.data.repository

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.data.remote.RedditApiService
import com.example.domain.model.MediaType
import com.example.domain.model.WallpaperItem
import com.example.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class WallpaperRepositoryImpl(
    private val context: Context,
    private val apiService: RedditApiService,
    private val subredditDao: SubredditDao,
    private val favoriteDao: FavoriteDao,
    private val viewedDao: ViewedDao,
    private val downloadDao: DownloadDao,
    private val blacklistDao: BlacklistDao
) : WallpaperRepository {

    override fun getFeed(
        subreddits: List<String>,
        sort: String,
        nsfwSetting: String,
        blacklist: List<BlacklistEntity>
    ): Flow<PagingData<WallpaperItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 25,
                enablePlaceholders = false,
                initialLoadSize = 25
            ),
            pagingSourceFactory = {
                RedditPagingSource(
                    context = context,
                    apiService = apiService,
                    subreddits = subreddits,
                    sort = sort,
                    nsfwSetting = nsfwSetting,
                    blacklist = blacklist,
                    favoriteDao = favoriteDao,
                    viewedDao = viewedDao
                )
            }
        ).flow
    }

    override fun getFavorites(): Flow<List<WallpaperItem>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.map { entity ->
                val maxDim = maxOf(entity.width, entity.height)
                val resTag = when {
                    maxDim >= 7680 -> "8K"
                    maxDim >= 3840 -> "4K"
                    maxDim >= 2560 -> "QHD"
                    maxDim >= 1920 -> "FHD"
                    else -> "HD"
                }
                WallpaperItem(
                    id = entity.postId,
                    title = entity.title,
                    subreddit = entity.subreddit,
                    score = entity.score,
                    isNsfw = entity.over18,
                    mediaUrl = entity.mediaUrl,
                    thumbnailUrl = entity.thumbnailUrl,
                    author = entity.author,
                    permalink = "https://www.reddit.com/r/${entity.subreddit}/comments/${entity.postId}",
                    createdUtc = entity.timestamp / 1000,
                    width = entity.width,
                    height = entity.height,
                    aspectRatio = entity.aspectRatio,
                    mediaType = MediaType.valueOf(entity.mediaType),
                    resolutionTag = resTag,
                    isFavorite = true,
                    isViewed = false
                )
            }
        }
    }

    override suspend fun getFavoritesSync(): List<FavoriteEntity> {
        return favoriteDao.getAllFavoritesSync()
    }

    override suspend fun addFavorite(item: WallpaperItem) {
        favoriteDao.insertFavorite(
            FavoriteEntity(
                postId = item.id,
                title = item.title,
                mediaUrl = item.mediaUrl,
                thumbnailUrl = item.thumbnailUrl,
                subreddit = item.subreddit,
                score = item.score,
                over18 = item.isNsfw,
                width = item.width,
                height = item.height,
                aspectRatio = item.aspectRatio,
                timestamp = System.currentTimeMillis(),
                author = item.author,
                mediaType = item.mediaType.name
            )
        )
    }

    override suspend fun removeFavorite(postId: String) {
        favoriteDao.deleteFavorite(postId)
    }

    override fun isFavorite(postId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(postId)
    }

    override fun getViewedHistory(): Flow<List<WallpaperItem>> {
        return viewedDao.getAllViewed().map { entities ->
            entities.map { entity ->
                WallpaperItem(
                    id = entity.postId,
                    title = entity.title,
                    subreddit = entity.subreddit,
                    score = 0,
                    isNsfw = false,
                    mediaUrl = entity.mediaUrl,
                    thumbnailUrl = entity.mediaUrl,
                    author = "",
                    permalink = "",
                    createdUtc = entity.timestamp / 1000,
                    width = 1080,
                    height = 2400,
                    aspectRatio = 0.56,
                    mediaType = MediaType.IMAGE,
                    resolutionTag = "FHD",
                    isFavorite = false,
                    isViewed = true,
                    viewedTimestamp = entity.timestamp
                )
            }
        }
    }

    override suspend fun addViewed(item: WallpaperItem) {
        viewedDao.insertViewed(
            ViewedEntity(
                postId = item.id,
                mediaUrl = item.mediaUrl,
                title = item.title,
                timestamp = System.currentTimeMillis(),
                subreddit = item.subreddit
            )
        )
    }

    override fun isViewed(postId: String): Flow<Boolean> {
        return viewedDao.isViewed(postId)
    }

    override suspend fun clearViewedHistory() {
        viewedDao.clearAllViewed()
    }

    override fun getSubreddits(): Flow<List<SubredditEntity>> {
        return subredditDao.getAllSubreddits()
    }

    override suspend fun getSubredditsSync(): List<SubredditEntity> {
        return subredditDao.getAllSubredditsSync()
    }

    override suspend fun insertSubreddit(subreddit: SubredditEntity) {
        subredditDao.insertSubreddit(subreddit)
    }

    override suspend fun deleteSubreddit(name: String) {
        subredditDao.deleteSubreddit(name)
    }

    override suspend fun updateSubreddit(subreddit: SubredditEntity) {
        subredditDao.updateSubreddit(subreddit)
    }

    override suspend fun resetDefaultSubreddits() {
        val defaults = listOf(
            SubredditEntity("wallpapers", 0, isEnabled = true, isCustom = false),
            SubredditEntity("mobilewallpapers", 1, isEnabled = true, isCustom = false),
            SubredditEntity("AmoledBackgrounds", 2, isEnabled = true, isCustom = false),
            SubredditEntity("EarthPorn", 3, isEnabled = true, isCustom = false),
            SubredditEntity("AnimeWallpaper", 4, isEnabled = true, isCustom = false),
            SubredditEntity("CityPorn", 5, isEnabled = true, isCustom = false),
            SubredditEntity("SpacePorn", 6, isEnabled = true, isCustom = false),
            SubredditEntity("ImaginaryLandscapes", 7, isEnabled = true, isCustom = false)
        )
        val current = subredditDao.getAllSubredditsSync()
        if (current.isEmpty()) {
            subredditDao.insertSubreddits(defaults)
        }
    }

    override fun getDownloads(): Flow<List<DownloadEntity>> {
        return downloadDao.getAllDownloads()
    }

    override suspend fun getDownloadsSync(): List<DownloadEntity> {
        return downloadDao.getAllDownloadsSync()
    }

    override suspend fun addDownload(download: DownloadEntity) {
        downloadDao.insertDownload(download)
    }

    override suspend fun updateDownload(
        downloadId: Long,
        status: String,
        progress: Int,
        filePath: String
    ) {
        downloadDao.updateDownloadStatus(downloadId, status, progress, filePath)
    }

    override suspend fun deleteDownload(downloadId: Long) {
        downloadDao.deleteDownload(downloadId)
    }

    override fun getBlacklist(): Flow<List<BlacklistEntity>> {
        return blacklistDao.getAllBlacklist()
    }

    override suspend fun addBlacklist(blacklist: BlacklistEntity) {
        blacklistDao.insertBlacklist(blacklist)
    }

    override suspend fun removeBlacklist(id: Int) {
        blacklistDao.deleteBlacklist(id)
    }
}
