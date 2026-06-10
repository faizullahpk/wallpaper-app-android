package com.example.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RedditResponse(
    @Json(name = "data") val data: RedditData
)

@JsonClass(generateAdapter = true)
data class RedditData(
    @Json(name = "children") val children: List<RedditChild>,
    @Json(name = "after") val after: String?
)

@JsonClass(generateAdapter = true)
data class RedditChild(
    @Json(name = "data") val data: RedditPostDto
)

@JsonClass(generateAdapter = true)
data class RedditPostDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "subreddit") val subreddit: String,
    @Json(name = "score") val score: Int,
    @Json(name = "over_18") val over18: Boolean,
    @Json(name = "url") val url: String,
    @Json(name = "thumbnail") val thumbnail: String,
    @Json(name = "author") val author: String,
    @Json(name = "permalink") val permalink: String,
    @Json(name = "created_utc") val createdUtc: Long,
    @Json(name = "is_video") val isVideo: Boolean,
    @Json(name = "post_hint") val postHint: String?,
    @Json(name = "preview") val preview: RedditPreview?,
    @Json(name = "media") val media: RedditMedia?
)

@JsonClass(generateAdapter = true)
data class RedditPreview(
    @Json(name = "images") val images: List<RedditPreviewImage>?
)

@JsonClass(generateAdapter = true)
data class RedditPreviewImage(
    @Json(name = "source") val source: RedditImageSource?,
    @Json(name = "resolutions") val resolutions: List<RedditImageSource>?
)

@JsonClass(generateAdapter = true)
data class RedditImageSource(
    @Json(name = "url") val url: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int
)

@JsonClass(generateAdapter = true)
data class RedditMedia(
    @Json(name = "reddit_video") val redditVideo: RedditVideo?
)

@JsonClass(generateAdapter = true)
data class RedditVideo(
    @Json(name = "fallback_url") val fallbackUrl: String?,
    @Json(name = "dash_url") val dashUrl: String?,
    @Json(name = "width") val width: Int?,
    @Json(name = "height") val height: Int?
)
