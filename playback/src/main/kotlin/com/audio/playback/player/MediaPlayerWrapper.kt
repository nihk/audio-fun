package com.audio.playback.player

import android.media.MediaPlayer

/**
 * There's no callback for play/pause and other commonly expected media information from the
 * MediaPlayer APIs. This class tries to fill in those gaps.
 */
internal class MediaPlayerWrapper(private val delegate: MediaPlayer) {
    interface Listener :
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {
        fun onPlayingChanged(isPlaying: Boolean)
    }

    private var internalListener: Listener? = null
    var listener: Listener?
        get() = internalListener
        set(value) {
            internalListener = value
            delegate.setOnCompletionListener(value)
        }

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
