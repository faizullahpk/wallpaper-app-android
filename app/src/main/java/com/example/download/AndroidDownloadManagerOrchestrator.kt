package com.example.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import com.example.data.local.entity.DownloadEntity
import com.example.domain.repository.WallpaperRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AndroidDownloadManagerOrchestrator(
    private val context: Context,
    private val repository: WallpaperRepository
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val scope = CoroutineScope(Dispatchers.IO)

    fun downloadMedia(postId: String, mediaUrl: String, title: String, subreddit: String) {
        val extension = when {
            mediaUrl.contains(".png", ignoreCase = true) -> "png"
            mediaUrl.contains(".webp", ignoreCase = true) -> "webp"
            mediaUrl.contains(".gif", ignoreCase = true) -> "gif"
            mediaUrl.contains(".mp4", ignoreCase = true) -> "mp4"
            else -> "jpg"
        }

        val cleanedTitle = title.replace(Regex("[^a-zA-Z0-9]"), "_").take(30)
        val fileName = "${cleanedTitle}_${postId}.$extension"

        val request = DownloadManager.Request(Uri.parse(mediaUrl))
            .setTitle("Downloading Wallpaper")
            .setDescription(title)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "RedditWallpapers/$fileName")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        try {
            val downloadId = downloadManager.enqueue(request)
            val path = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "RedditWallpapers/$fileName"
            ).absolutePath

            scope.launch {
                repository.addDownload(
                    DownloadEntity(
                        downloadId = downloadId,
                        postId = postId,
                        title = title,
                        mediaUrl = mediaUrl,
                        subreddit = subreddit,
                        filePath = path,
                        status = "RUNNING",
                        progress = 0,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registerReceiver() {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(DownloadBroadcastReceiver(), filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(DownloadBroadcastReceiver(), filter)
        }
    }

    inner class DownloadBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != -1L) {
                scope.launch {
                    val query = DownloadManager.Query().setFilterById(id)
                    val cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                        val status = if (statusIndex != -1) cursor.getInt(statusIndex) else DownloadManager.STATUS_FAILED
                        val localUriString = if (uriIndex != -1) cursor.getString(uriIndex) else ""

                        val dbStatus = when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> "SUCCESSFUL"
                            DownloadManager.STATUS_FAILED -> "FAILED"
                            DownloadManager.STATUS_RUNNING -> "RUNNING"
                            else -> "PENDING"
                        }

                        repository.updateDownload(
                            downloadId = id,
                            status = dbStatus,
                            progress = if (dbStatus == "SUCCESSFUL") 100 else 0,
                            filePath = localUriString
                        )
                        cursor.close()
                    }
                }
            }
        }
    }
}
