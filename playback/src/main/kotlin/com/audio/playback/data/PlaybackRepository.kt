package com.audio.playback.data

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface PlaybackRepository {
    fun play(filename: String): Flow<Unit>
}

class MediaPlayerPlaybackRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PlaybackRepository {
    override fun play(filename: String): Flow<Unit> = callbackFlow {
        Log.d("asdf", "starting MediaPlayer playback")
        val player: MediaPlayer = MediaPlayer.create(context, filename.toUri())
        player.start()

        awaitClose {
            Log.d("asdf", "tearing down MediaPlayer")
            player.stop()
            player.reset()
            player.release()
        }
    }
}
