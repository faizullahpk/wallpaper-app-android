package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subreddits")
data class SubredditEntity(
    @PrimaryKey val name: String,
    val orderIndex: Int,
    val isEnabled: Boolean,
    val isCustom: Boolean
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val postId: String,
    val title: String,
    val mediaUrl: String,
    val thumbnailUrl: String,
    val subreddit: String,
    val score: Int,
    val over18: Boolean,
    val width: Int,
    val height: Int,
    val aspectRatio: Double,
    val timestamp: Long,
    val author: String,
    val mediaType: String // "IMAGE", "VIDEO", "GIF"
)

@Entity(tableName = "viewed_history")
data class ViewedEntity(
    @PrimaryKey val postId: String,
    val mediaUrl: String,
    val title: String,
    val timestamp: Long,
    val subreddit: String
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val downloadId: Long, // Android DownloadManager reference ID
    val postId: String,
    val title: String,
    val mediaUrl: String,
    val subreddit: String,
    val filePath: String,
    val status: String, // "PENDING", "RUNNING", "SUCCESSFUL", "FAILED"
    val progress: Int,
    val timestamp: Long
)

@Entity(tableName = "blacklist")
data class BlacklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "SUBREDDIT", "KEYWORD", "FLAIR", "TITLE"
    val value: String
)
