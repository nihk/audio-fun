package com.audio.playback.player

import android.media.MediaPlayer

internal class MediaPlayerWrapper(private val delegate: MediaPlayer) {
    interface Listener :
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {
        fun onPlayingChanged(isPlaying: Boolean)
    }
    var listener: Listener? = null

    fun start() {
        delegate.start()
        listener?.onPlayingChanged(delegate.isPlaying)
    }

    fun pause() {
        delegate.pause()
        listener?.onPlayingChanged(delegate.isPlaying)
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
