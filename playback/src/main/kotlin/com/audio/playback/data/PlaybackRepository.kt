package com.audio.playback.data

import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import com.audio.core.ui.ApplicationContext
import com.audio.playback.player.MediaPlayerWrapper
import com.audio.playback.player.wrap
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal interface PlaybackRepository {
    suspend fun create(filename: String, play: Boolean): Flow<Emission>
    suspend fun play()
    suspend fun pause()

    sealed class Emission {
        object Created : Emission()
        data class PlayingStateChanged(val isPlaying: Boolean) : Emission()
        data class Error(val what: Int): Emission()
    }
}

internal class MediaPlayerPlaybackRepository @Inject constructor(
    private val context: ApplicationContext
) : PlaybackRepository {
    private var player: MediaPlayerWrapper? = null

    override suspend fun create(filename: String, play: Boolean) = callbackFlow {
        check(player == null)
        val player = MediaPlayer.create(context, filename.toUri())
            .wrap()
            .also { this@MediaPlayerPlaybackRepository.player = it }
        trySend(PlaybackRepository.Emission.Created)

        val listener = object : MediaPlayerWrapper.Listener {
            override fun onPlayingChanged(isPlaying: Boolean) {
                trySend(PlaybackRepository.Emission.PlayingStateChanged(isPlaying))
            }

            override fun onCompletion(mp: MediaPlayer) {
                trySend(PlaybackRepository.Emission.PlayingStateChanged(false))
            }

            override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
                trySend(PlaybackRepository.Emission.Error(what))
                return false // todo
            }
        }

        player.listener = listener

        if (play) {
            play()
        }

        awaitClose {
            Log.d("asdf", "tearing down player")
            with(player) {
                this@with.listener = null
                stop()
                reset()
                release()
            }
            this@MediaPlayerPlaybackRepository.player = null
        }
    }

    override suspend fun play() {
        Log.d("asdf", "starting playback")
        requireNotNull(player).start()
    }

    override suspend fun pause() {
        Log.d("asdf", "pausing playback")
        requireNotNull(player).pause()
    }
}
