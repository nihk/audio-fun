package com.audio.recordings.ui

import com.audio.core.mvi.MviViewModel
import com.audio.recordings.data.Effect
import com.audio.recordings.data.Event
import com.audio.recordings.data.RecordingsRepository
import com.audio.recordings.data.Result
import com.audio.recordings.data.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

@HiltViewModel
class RecordingsViewModel @Inject constructor(
    private val repository: RecordingsRepository
) : MviViewModel<Event, Result, State, Effect>(State()) {
    override fun onStart() {
        processEvent(Event.ShowRecordingsEvent)
    }

    override fun Result.reduce(state: State): State {
        return when (this) {
            is Result.ShowRecordingsResult -> state.copy(recordings = recordings)
            else -> state
        }
    }

    override fun Flow<Event>.toResults(): Flow<Result> {
        return merge(
            filterIsInstance<Event.ShowRecordingsEvent>().toShowRecordingsResults(),
            filterIsInstance<Event.DeleteRecordingEvent>().toDeleteRecordingResults(),
            filterIsInstance<Event.ToRecorderEvent>().toRecordResults()
        )
    }

    private fun Flow<Event.ShowRecordingsEvent>.toShowRecordingsResults(): Flow<Result> {
        return flatMapLatest { repository.recordings() }
            .map(Result::ShowRecordingsResult)
    }

    private fun Flow<Event.DeleteRecordingEvent>.toDeleteRecordingResults(): Flow<Result> {
        return transform { event -> repository.delete(event.recording) }
    }

    private fun Flow<Event.ToRecorderEvent>.toRecordResults(): Flow<Result> {
        return mapLatest { Result.EffectResult(Effect.NavigateToRecorderEffect) }
    }

    override fun Flow<Result>.toEffects(): Flow<Effect> {
        return merge(
            filterIsInstance<Result.EffectResult>().toResultEffects()
        )
    }

    private fun Flow<Result.EffectResult>.toResultEffects(): Flow<Effect> {
        return mapLatest { result -> result.effect }
    }
}
