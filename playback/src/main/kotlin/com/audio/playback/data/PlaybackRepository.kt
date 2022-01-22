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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

interface PlaybackRepository {
    fun emissions(): Flow<Emission>
    suspend fun create(filename: String)
    suspend fun play()
    suspend fun stop()
    suspend fun pause()

    sealed class Emission {
        object Created : Emission()
        data class PlayingStateChanged(val isPlaying: Boolean) : Emission()
        object Stopped : Emission()
    }
}

class MediaPlayerPlaybackRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PlaybackRepository {
    private val events = MutableSharedFlow<Event>()
    private var player: MediaPlayerWrapper? = null // fixme: maybe create and set the value of this field within the callbackFlow? and use a CompletableDeferred?

    override fun emissions(): Flow<PlaybackRepository.Emission> = events.flatMapLatest { event ->
        when (event) {
            Event.Created -> playerEventStream()
            Event.Released -> flowOf<PlaybackRepository.Emission>(PlaybackRepository.Emission.Stopped)
        }
    }

    private fun playerEventStream(): Flow<PlaybackRepository.Emission> = callbackFlow {
        val player = requireNotNull(player)
        trySend(PlaybackRepository.Emission.Created)

        val listener: (isPlaying: Boolean) -> Unit = { isPlaying ->
            trySend(PlaybackRepository.Emission.PlayingStateChanged(isPlaying))
        }

        player.setIsPlayingListener(listener)

        awaitClose {
            Log.d("asdf", "unlistening to player")
            player.setIsPlayingListener(null)
        }
    }

    override suspend fun create(filename: String) {
        Log.d("asdf", "creating MediaPlayer")
        check(player == null)
        player = MediaPlayer.create(context, filename.toUri()).wrap()
        events.emit(Event.Created)
    }

    override suspend fun play() {
        Log.d("asdf", "starting playback")
        requireNotNull(player).start()
    }

    override suspend fun pause() {
        Log.d("asdf", "pausing playback")
        requireNotNull(player).pause()
    }

    override suspend fun stop() {
        Log.d("asdf", "tearing down MediaPlayer")
        with(requireNotNull(player)) {
            stop()
            reset()
            release()
        }

        player = null
        events.emit(Event.Released)
    }

    private sealed class Event {
        object Created : Event()
        object Released : Event()
    }
}
