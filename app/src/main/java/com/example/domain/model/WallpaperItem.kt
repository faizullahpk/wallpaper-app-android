package com.example.domain.model

data class WallpaperItem(
    val id: String,
    val title: String,
    val subreddit: String,
    val score: Int,
    val isNsfw: Boolean,
    val mediaUrl: String,
    val thumbnailUrl: String,
    val author: String,
    val permalink: String,
    val createdUtc: Long,
    val width: Int,
    val height: Int,
    val aspectRatio: Double,
    val mediaType: MediaType,
    val resolutionTag: String,
    val isFavorite: Boolean = false,
    val isViewed: Boolean = false,
    val viewedTimestamp: Long? = null
)

enum class MediaType {
    IMAGE,
    VIDEO,
    GIF
}
