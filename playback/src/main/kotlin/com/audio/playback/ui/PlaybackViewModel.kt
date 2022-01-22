package com.audio.playback.ui

import androidx.lifecycle.SavedStateHandle
import com.audio.core.extensions.requireNotNull
import com.audio.core.mvi.MviViewModel
import com.audio.playback.data.Effect
import com.audio.playback.data.Event
import com.audio.playback.data.PlaybackRepository
import com.audio.playback.data.Result
import com.audio.playback.data.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

@HiltViewModel
internal class PlaybackViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val repository: PlaybackRepository
) : MviViewModel<Event, Result, State, Effect>(State()) {
    override suspend fun onSubscription() {
        processEvent(Event.CreatePlayerEvent(start = true))
    }

    override fun Result.reduce(state: State): State {
        return when (this) {
            is Result.PlayingStateChangedResult -> state.copy(isPlaying = isPlaying)
            else -> state
        }
    }

    override fun Flow<Event>.toResults(): Flow<Result> {
        return merge(
            filterIsInstance<Event.CreatePlayerEvent>().toCreatePlayerResults(),
            filterIsInstance<Event.Play>().transform { repository.play() },
            filterIsInstance<Event.Pause>().transform { repository.pause() }
        )
    }

    private fun Flow<Event.CreatePlayerEvent>.toCreatePlayerResults(): Flow<Result> {
        return flatMapLatest { event ->
            val recordingName = handle.get<String>(PlaybackFragment.KEY_RECORDING_NAME).requireNotNull()
            repository.create(recordingName, event.start)
        }.map { emission ->
            when (emission) {
                PlaybackRepository.Emission.Created -> Result.NoOpResult
                is PlaybackRepository.Emission.PlayingStateChanged -> Result.PlayingStateChangedResult(emission.isPlaying)
            }
        }
    }

    override fun Flow<Result>.toEffects(): Flow<Effect> {
        return merge(
        )
    }
}
