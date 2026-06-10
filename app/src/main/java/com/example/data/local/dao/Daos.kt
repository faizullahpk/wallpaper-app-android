package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubredditDao {
    @Query("SELECT * FROM subreddits ORDER BY orderIndex ASC")
    fun getAllSubreddits(): Flow<List<SubredditEntity>>

    @Query("SELECT * FROM subreddits ORDER BY orderIndex ASC")
    suspend fun getAllSubredditsSync(): List<SubredditEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubreddits(subreddits: List<SubredditEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubreddit(subreddit: SubredditEntity)

    @Query("DELETE FROM subreddits WHERE name = :name")
    suspend fun deleteSubreddit(name: String)

    @Update
    suspend fun updateSubreddit(subreddit: SubredditEntity)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    suspend fun getAllFavoritesSync(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE postId = :postId")
    suspend fun deleteFavorite(postId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE postId = :postId)")
    fun isFavorite(postId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE postId = :postId)")
    suspend fun isFavoriteSync(postId: String): Boolean

    @Query("SELECT * FROM favorites WHERE title LIKE '%' || :query || '%' OR subreddit LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchFavorites(query: String): Flow<List<FavoriteEntity>>
}

@Dao
interface ViewedDao {
    @Query("SELECT * FROM viewed_history ORDER BY timestamp DESC")
    fun getAllViewed(): Flow<List<ViewedEntity>>

    @Query("SELECT * FROM viewed_history ORDER BY timestamp DESC")
    suspend fun getAllViewedSync(): List<ViewedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViewed(viewed: ViewedEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM viewed_history WHERE postId = :postId)")
    fun isViewed(postId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM viewed_history WHERE postId = :postId)")
    suspend fun isViewedSync(postId: String): Boolean

    @Query("DELETE FROM viewed_history")
    suspend fun clearAllViewed()
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    suspend fun getAllDownloadsSync(): List<DownloadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status, progress = :progress, filePath = :filePath WHERE downloadId = :downloadId")
    suspend fun updateDownloadStatus(downloadId: Long, status: String, progress: Int, filePath: String)

    @Query("DELETE FROM downloads WHERE downloadId = :downloadId")
    suspend fun deleteDownload(downloadId: Long)

    @Query("SELECT * FROM downloads WHERE downloadId = :downloadId")
    suspend fun getDownload(downloadId: Long): DownloadEntity?
}

@Dao
interface BlacklistDao {
    @Query("SELECT * FROM blacklist ORDER BY id DESC")
    fun getAllBlacklist(): Flow<List<BlacklistEntity>>

    @Query("SELECT * FROM blacklist ORDER BY id DESC")
    suspend fun getAllBlacklistSync(): List<BlacklistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlacklist(blacklist: BlacklistEntity)

    @Query("DELETE FROM blacklist WHERE id = :id")
    suspend fun deleteBlacklist(id: Int)
}
