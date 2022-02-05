package com.audio.playback.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.audio.core.mvi.MviViewModel
import com.audio.playback.data.Effect
import com.audio.playback.data.Event
import com.audio.playback.data.PlaybackRepository
import com.audio.playback.data.Result
import com.audio.playback.data.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

internal class PlaybackViewModel(
    private val handle: SavedStateHandle,
    private val repository: PlaybackRepository,
    private val recordingName: String
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
            repository.create(recordingName, event.start)
        }.map { emission ->
            when (emission) {
                PlaybackRepository.Emission.Created -> Result.NoOpResult
                is PlaybackRepository.Emission.PlayingStateChanged -> Result.PlayingStateChangedResult(emission.isPlaying)
                is PlaybackRepository.Emission.Error -> Result.EffectResult(Effect.ErrorEffect(emission.what))
            }
        }
    }

    override fun Flow<Result>.toEffects(): Flow<Effect> {
        return merge(
            filterIsInstance<Result.EffectResult>().map { result -> result.effect }
        )
    }

    class Factory(
        private val repository: PlaybackRepository,
        private val recordingName: String
    ) {
        fun create(
            owner: SavedStateRegistryOwner
        ): ViewModelProvider.Factory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return PlaybackViewModel(handle, repository, recordingName) as T
                }
            }
        }
    }
}
