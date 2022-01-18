package com.audio.playback.data

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
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
        object Playing : Emission()
        object Paused : Emission()
        object Stopped : Emission()
        object Completed : Emission()
        data class Error(val what: Int) : Emission()
    }
}

class MediaPlayerPlaybackRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PlaybackRepository {
    private val events = MutableSharedFlow<Event>()
    private var player: MediaPlayer? = null

    override fun emissions(): Flow<PlaybackRepository.Emission> = events.flatMapLatest { event ->
        when (event) {
            Event.Created -> playerEventStream()
            Event.Released -> flowOf<PlaybackRepository.Emission>(PlaybackRepository.Emission.Paused)
        }
    }

    private fun playerEventStream(): Flow<PlaybackRepository.Emission> = callbackFlow {
        val player = requireNotNull(player)
        trySend(PlaybackRepository.Emission.Created)

        val listener = object :
            MediaPlayer.OnInfoListener,
            MediaPlayer.OnPreparedListener,
            MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener {

            override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
                Log.d("asdf", "info: $what, extra: $extra")
                // todo: emit here
                return false
            }

            override fun onPrepared(mp: MediaPlayer) {
                Log.d("asdf", "prepared player")
            }

            override fun onCompletion(mp: MediaPlayer) {
                // fixme: not getting called back with aac file?
                Log.d("asdf", "completed")
                trySend(PlaybackRepository.Emission.Completed)
            }

            override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
                trySend(PlaybackRepository.Emission.Error(what))
                return false // todo
            }
        }

        with(player) {
            setOnInfoListener(listener)
            setOnPreparedListener(listener)
            setOnErrorListener(listener)
        }

        awaitClose {
            Log.d("asdf", "unlistening to player")
            with(player) {
                setOnInfoListener(null)
                setOnPreparedListener(null)
                setOnErrorListener(null)
            }
        }
    }

    override suspend fun create(filename: String) {
        Log.d("asdf", "creating MediaPlayer")
        check(player == null)
        player = MediaPlayer.create(context, filename.toUri())
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
