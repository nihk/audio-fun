package com.audio.playback.data

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import com.audio.playback.player.MediaPlayerWrapper
import com.audio.playback.player.wrap
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface PlaybackRepository {
    suspend fun create(filename: String, play: Boolean): Flow<Emission>
    suspend fun play()
    suspend fun pause()

    sealed class Emission {
        object Created : Emission()
        data class PlayingStateChanged(val isPlaying: Boolean) : Emission()
    }
}

class MediaPlayerPlaybackRepository @Inject constructor(
    @ApplicationContext private val context: Context
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
