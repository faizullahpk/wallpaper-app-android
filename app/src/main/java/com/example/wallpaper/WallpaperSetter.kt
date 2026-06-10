package com.example.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import androidx.annotation.WorkerThread
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class WallpaperSetter(private val context: Context) {

    private val wallpaperManager = WallpaperManager.getInstance(context)

    suspend fun setWallpaperFromUrl(
        url: String,
        screenType: ScreenType,
        scaleMode: ScaleMode
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Required to convert to bytes/bitmap safely
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    val finalBitmap = scaleBitmapToScreen(bitmap, scaleMode)
                    setWallpaper(finalBitmap, screenType)
                    return@withContext true
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @WorkerThread
    private fun scaleBitmapToScreen(source: Bitmap, scaleMode: ScaleMode): Bitmap {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val sourceWidth = source.width
        val sourceHeight = source.height

        val screenAspect = screenWidth.toFloat() / screenHeight.toFloat()
        val sourceAspect = sourceWidth.toFloat() / sourceHeight.toFloat()

        return when (scaleMode) {
            ScaleMode.FILL -> {
                // Scale so that it covers the entire screen, crop excess sides
                val scale: Float
                var dx = 0f
                var dy = 0f

                if (sourceAspect > screenAspect) {
                    scale = screenHeight.toFloat() / sourceHeight.toFloat()
                    dx = (screenWidth - sourceWidth * scale) * 0.5f
                } else {
                    scale = screenWidth.toFloat() / sourceWidth.toFloat()
                    dy = (screenHeight - sourceHeight * scale) * 0.5f
                }

                val matrix = Matrix().apply {
                    postScale(scale, scale)
                    postTranslate(dx, dy)
                }

                val dest = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(dest)
                canvas.drawBitmap(source, matrix, null)
                dest
            }

            ScaleMode.FIT -> {
                // Scale so that everything fits inside the screen, letterbox/pillarbox empty spaces
                val scale: Float
                var dx = 0f
                var dy = 0f

                if (sourceAspect > screenAspect) {
                    scale = screenWidth.toFloat() / sourceWidth.toFloat()
                    dy = (screenHeight - sourceHeight * scale) * 0.5f
                } else {
                    scale = screenHeight.toFloat() / sourceHeight.toFloat()
                    dx = (screenWidth - sourceWidth * scale) * 0.5f
                }

                val matrix = Matrix().apply {
                    postScale(scale, scale)
                    postTranslate(dx, dy)
                }

                val dest = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(dest)
                canvas.drawColor(Color.BLACK) // Letterbox background color
                canvas.drawBitmap(source, matrix, null)
                dest
            }

            ScaleMode.CROP -> {
                // Default custom scale or center crop returning original if sizing matches
                source
            }
        }
    }

    private fun setWallpaper(bitmap: Bitmap, screenType: ScreenType) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val flag = when (screenType) {
                    ScreenType.HOME -> WallpaperManager.FLAG_SYSTEM
                    ScreenType.LOCK -> WallpaperManager.FLAG_LOCK
                    ScreenType.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, flag)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    enum class ScreenType {
        HOME, LOCK, BOTH
    }

    enum class ScaleMode {
        FIT, FILL, CROP
    }
}
