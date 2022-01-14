package com.audio.recordings.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.audio.core.mvi.MviViewModel
import com.audio.recordings.data.Effect
import com.audio.recordings.data.Event
import com.audio.recordings.data.Result
import com.audio.recordings.data.State
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge

class RecordingsViewModel : MviViewModel<Event, Result, State, Effect>(State()) {
    override fun Result.reduce(state: State): State {
        return state
    }

    override fun Flow<Event>.toResults(): Flow<Result> {
        return merge(
            filterIsInstance<Event.RecordEvent>().toRecordResults()
        )
    }

    private fun Flow<Event.RecordEvent>.toRecordResults(): Flow<Result> {
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

    class Factory @Inject constructor() {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return RecordingsViewModel() as T
                }
            }
        }
    }
}
