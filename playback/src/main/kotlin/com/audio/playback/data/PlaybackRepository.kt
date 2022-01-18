package com.audio.playback.data

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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

    private fun playerEventStream(): Flow<PlaybackRepository.Emission> = flow {
        val player = requireNotNull(player)
        emit(PlaybackRepository.Emission.Created)

        val listener = object :
            MediaPlayer.OnInfoListener,
            MediaPlayer.OnPreparedListener {

            override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
                Log.d("asdf", "info: $what, extra: $extra")
                // todo: emit here
                return false
            }

            override fun onPrepared(mp: MediaPlayer) {
                Log.d("asdf", "prepared player")
            }
        }

        with(player) {
            setOnInfoListener(listener)
            setOnPreparedListener(listener)
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
        val player = requireNotNull(player)
        player.start()
    }

    override suspend fun pause() {
        Log.d("asdf", "pausing playback")
        requireNotNull(player).pause()
    }

    override suspend fun stop() {
        Log.d("asdf", "tearing down MediaPlayer")
        val player = requireNotNull(player)
        player.setOnInfoListener(null)
        player.setOnPreparedListener(null)
        player.stop()
        player.reset()
        player.release()
        this.player = null
        events.emit(Event.Released)
    }

    private sealed class Event {
        object Created : Event()
        object Released : Event()
    }
}
