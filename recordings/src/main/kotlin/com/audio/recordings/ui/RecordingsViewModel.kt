package com.audio.recordings.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.audio.core.mvi.MviViewModel
import com.audio.recordings.data.Effect
import com.audio.recordings.data.Event
import com.audio.recordings.data.RecordingsRepository
import com.audio.recordings.data.Result
import com.audio.recordings.data.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

internal class RecordingsViewModel(
    private val repository: RecordingsRepository
) : MviViewModel<Event, Result, State, Effect>(State()) {
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
            filterIsInstance<Event.ToPlaybackEvent>().toPlaybackResults(),
            filterIsInstance<Event.ToRecorderEvent>().toRecordResults()
        )
    }

    private fun Flow<Event.ShowRecordingsEvent>.toShowRecordingsResults(): Flow<Result> {
        return distinctUntilChanged()
            .flatMapLatest { event ->
                when (event.action) {
                    Event.ShowRecordingsEvent.Action.Start -> repository.recordings()
                    Event.ShowRecordingsEvent.Action.Stop -> emptyFlow()
                }
            }
            .map(Result::ShowRecordingsResult)
    }

    private fun Flow<Event.DeleteRecordingEvent>.toDeleteRecordingResults(): Flow<Result> {
        return transform { event -> repository.delete(event.recording) }
    }

    private fun Flow<Event.ToPlaybackEvent>.toPlaybackResults(): Flow<Result> {
        return mapLatest { event ->
            Result.EffectResult(Effect.NavigateToPlaybackEffect(event.recording.absolute))
        }
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

    class Factory(
        private val repository: RecordingsRepository
    ) {
        fun create(owner: SavedStateRegistryOwner): ViewModelProvider.Factory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return RecordingsViewModel(repository) as T
                }
            }
        }
    }
}
