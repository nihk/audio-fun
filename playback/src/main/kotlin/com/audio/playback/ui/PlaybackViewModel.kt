package com.audio.playback.ui

import android.util.Log
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
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val repository: PlaybackRepository
) : MviViewModel<Event, Result, State, Effect>(State()) {
    override suspend fun onSubscription() {
        processEvent(Event.ListenToPlayerEvent)
    }

    override suspend fun onCompletion() {
        repository.stop()
    }

    override fun Result.reduce(state: State): State {
        return state.copy()
    }

    override fun Flow<Event>.toResults(): Flow<Result> {
        return merge(
            filterIsInstance<Event.ListenToPlayerEvent>().toStartPlayerResults(),
            filterIsInstance<Event.CreatePlayerEvent>().toCreatePlayerResults()
        )
    }

    private fun Flow<Event.ListenToPlayerEvent>.toStartPlayerResults(): Flow<Result> {
        return flatMapLatest { event -> repository.emissions() }
            .mapLatest { emission ->
                // todo: do something with emissions
                Log.d("asdf", "got emission: $emission")
                Result.NoOpResult
            }
            .onStart<Result> {
                emit(Result.EffectResult(Effect.ListeningToPlayerEffect))
            }
    }

    private fun Flow<Event.CreatePlayerEvent>.toCreatePlayerResults(): Flow<Result> {
        return transformLatest { event ->
            val recordingName = handle.get<String>(PlaybackFragment.KEY_RECORDING_NAME).requireNotNull()
            repository.create(recordingName)
            if (event.start) {
                repository.play()
            }
        }
    }

    override fun Flow<Result>.toEffects(): Flow<Effect> {
        return merge(
            filterIsInstance<Result.EffectResult>().toResultEffects(),
        )
    }

    private fun Flow<Result.EffectResult>.toResultEffects(): Flow<Effect> {
        return mapLatest { result -> result.effect }
    }
}
