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
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val repository: PlaybackRepository
) : MviViewModel<Event, Result, State, Effect>(State()) {
    override fun onStart() {
        val recordingName = handle.get<String>(PlaybackFragment.KEY_RECORDING_NAME).requireNotNull()
        processEvent(Event.StartPlaybackEvent(recordingName))
    }

    override fun Result.reduce(state: State): State {
        return state
    }

    override fun Flow<Event>.toResults(): Flow<Result> {
        return merge(
            filterIsInstance<Event.StartPlaybackEvent>().toStartPlaybackResults()
        )
    }

    private fun Flow<Event.StartPlaybackEvent>.toStartPlaybackResults(): Flow<Result> {
        return flatMapLatest { event -> repository.play(event.recordingName) }
            .transform {
                // todo: some kind of feedback from playback
            }
    }
}
