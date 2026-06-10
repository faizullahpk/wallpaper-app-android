package com.example.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.WallpaperApplication
import com.example.wallpaper.WallpaperSetter
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class AutoWallpaperWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? WallpaperApplication ?: return Result.failure()
        val container = app.container

        try {
            val source = container.settingsManager.autoWallpaperSource.first()
            val wallpaperSetter = container.wallpaperSetter

            val candidates = when (source) {
                "FAVORITES" -> {
                    container.repository.getFavoritesSync().map { it.mediaUrl }
                }
                "DOWNLOADS" -> {
                    container.repository.getDownloadsSync()
                        .filter { it.status == "SUCCESSFUL" }
                        .map { it.mediaUrl }
                }
                else -> { // "SUBREDDITS"
                    val subs = container.repository.getSubredditsSync()
                        .filter { it.isEnabled }
                        .map { it.name }
                    if (subs.isNotEmpty()) {
                        val randomSub = subs[Random.nextInt(subs.size)]
                        val url = "https://rl.bloat.cat/r/$randomSub/hot.json?limit=25&raw_json=1"
                        val request = okhttp3.Request.Builder()
                            .url(url)
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                            .build()
                        val client = okhttp3.OkHttpClient.Builder()
                            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                            .build()
                        val jsonString = client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) {
                                throw Exception("HTTP Error: ${response.code}")
                            }
                            response.body?.string() ?: ""
                        }
                        val moshi = com.squareup.moshi.Moshi.Builder().build()
                        val adapter = moshi.adapter(com.example.data.remote.model.RedditResponse::class.java)
                        val response = adapter.fromJson(jsonString) ?: throw Exception("Failed to parse RedditResponse JSON")
                        
                        response.data.children.map { it.data.url }.filter {
                            it.endsWith(".png", true) ||
                            it.endsWith(".jpg", true) ||
                            it.endsWith(".jpeg", true) ||
                            it.endsWith(".webp", true)
                        }
                    } else {
                        emptyList()
                    }
                }
            }

            if (candidates.isEmpty()) return Result.success()

            val randomUrl = candidates[Random.nextInt(candidates.size)]
            val success = wallpaperSetter.setWallpaperFromUrl(
                url = randomUrl,
                screenType = WallpaperSetter.ScreenType.BOTH,
                scaleMode = WallpaperSetter.ScaleMode.FILL
            )

            return if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
