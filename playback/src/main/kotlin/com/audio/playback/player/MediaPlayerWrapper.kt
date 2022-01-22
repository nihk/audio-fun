package com.audio.playback.player

import android.media.MediaPlayer

class MediaPlayerWrapper(private val delegate: MediaPlayer) {
    private var isPlayingListener: ((Boolean) -> Unit)? = null

    val isPlaying: Boolean get() = delegate.isPlaying

    fun setIsPlayingListener(isPlayingListener: ((Boolean) -> Unit)?) {
        this.isPlayingListener = isPlayingListener
    }

    fun start() {
        delegate.start()
        isPlayingListener?.invoke(delegate.isPlaying)
    }

    fun pause() {
        delegate.pause()
        isPlayingListener?.invoke(delegate.isPlaying)
    }

    fun stop() {
        delegate.stop()
    }

    fun reset() {
        delegate.reset()
    }

    fun release() {
        delegate.release()
    }
}

internal fun MediaPlayer.wrap(): MediaPlayerWrapper = MediaPlayerWrapper(this)
