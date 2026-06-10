package com.example.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

class WallpaperVideoPlayer(private val context: Context) {

    private var player: ExoPlayer? = null

    fun getPlayer(): Player {
        return player ?: ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            player = this
        }
    }

    @OptIn(UnstableApi::class)
    fun playMedia(url: String, isMuted: Boolean = true) {
        val exo = player ?: ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            player = this
        }

        val mediaItem = MediaItem.fromUri(url)
        exo.setMediaItem(mediaItem)
        exo.prepare()
        setMute(isMuted)
    }

    fun setMute(isMuted: Boolean) {
        player?.volume = if (isMuted) 0f else 1f
    }

    fun play() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
    }

    fun release() {
        player?.stop()
        player?.release()
        player = null
    }
}
