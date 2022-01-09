package nick.template.ui

import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import nick.template.data.AudioRepository
import nick.template.data.CachedFilenameHandle
import nick.template.data.Effect
import nick.template.data.Event
import nick.template.data.Result
import nick.template.data.State

class MainViewModel(
    private val handle: CachedFilenameHandle,
    private val audioRepository: AudioRepository,
) : MviViewModel<Event, Result, State, Effect>(State()) {

    override fun onStart() {
        processEvent(Event.RequestPermissionEvent.FromCreation)
    }

    override fun Result.reduce(state: State): State {
        return when (this) {
            is Result.StartRecordingResult -> state.copy(cachedFilename = cachedFilename)
            is Result.CachedRecordingClearedResult -> state.copy(cachedFilename = null)
            is Result.RequestPermissionResult.FromStartRecording -> state.copy(startRecordingAfterPermissionGranted = true)
            else -> state
        }
    }

    override fun Flow<Event>.toResults(): Flow<Result> {
        return merge(
            filterIsInstance<Event.RequestPermissionEvent>().toRequestPermissionResults(),
            filterIsInstance<Event.PermissionResultEvent>().toPermissionResults(),
            filterIsInstance<Event.RecordEvent>().toRecordingResults(),
            filterIsInstance<Event.SaveRecordingEvent>().toSaveRecordingResults(),
            filterIsInstance<Event.CancelSaveRecordingEvent>().toCancelSaveRecordingResults()
        )
    }

    private fun Flow<Event.RequestPermissionEvent>.toRequestPermissionResults(): Flow<Result> {
        return mapLatest { event ->
            when (event) {
                Event.RequestPermissionEvent.FromCreation -> Result.RequestPermissionResult.FromCreation
                Event.RequestPermissionEvent.FromStartRecording -> Result.RequestPermissionResult.FromStartRecording
            }
        }
    }

    private fun Flow<Event.PermissionResultEvent>.toPermissionResults(): Flow<Result> {
        return mapLatest { event ->
            when (event) {
                // todo: punt user to settings if they've requested too often; check if shouldShowRequestPermissionRationale
                Event.PermissionResultEvent.Denied -> {
                    Log.d("asdf", "permission denied")
                    Result.NoOpResult
                }
                Event.PermissionResultEvent.Granted -> if (states.value.startRecordingAfterPermissionGranted) {
                    Log.d("asdf", "permission granted; starting recording")
                    Result.EffectResult(Effect.StartRecordingEffect)
                } else {
                    Result.NoOpResult // Granted permission from the initial screen creation prompt
                }
            }
        }
    }

    private fun Flow<Event.RecordEvent>.toRecordingResults(): Flow<Result> {
        // todo: make sure recording twice without stopping is never permitted
        return flatMapLatest { event ->
            when (event) {
                Event.RecordEvent.Start -> {
                    audioRepository.record().map { emission ->
                        when (emission) {
                            is AudioRepository.Emission.Error -> Result.ErrorRecordingResult(emission.throwable)
                            is AudioRepository.Emission.Recording -> {
                                handle.filename = emission.cachedFilename
                                Result.StartRecordingResult(
                                    emission.cachedFilename
                                )
                            }
                            is AudioRepository.Emission.Amplitude -> {
                                Log.d("asdf", "amplitude: ${emission.value}")
                                Result.NoOpResult
                            }
                        }
                    }
                }
                Event.RecordEvent.Stop -> {
                    val cachedFilename = handle.filename
                    val result = if (cachedFilename != null) {
                        Result.StopRecordingResult(cachedFilename)
                    } else {
                        Result.NoOpResult // An error happened previously, or recording didn't start yet; nothing to do here.
                    }
                    flowOf(result)
                }
            }
        }
    }

    private fun Flow<Event.SaveRecordingEvent>.toSaveRecordingResults(): Flow<Result> {
        return mapLatest { event ->
            val cachedFilename = handle.require()
            audioRepository.save(
                cachedFilename = cachedFilename,
                destinationFilename = event.filename,
                copyToMusicFolder = event.copyToMusicFolder
            )
            audioRepository.deleteFromCache(cachedFilename)
            handle.filename = null
            Result.CachedRecordingClearedResult
        }
    }

    private fun Flow<Event.CancelSaveRecordingEvent>.toCancelSaveRecordingResults(): Flow<Result> {
        return mapLatest {
            audioRepository.deleteFromCache(handle.require())
            handle.filename = null
            Result.CachedRecordingClearedResult
        }
    }

    override fun Flow<Result>.toEffects(): Flow<Effect> {
        return merge(
            filterIsInstance<Result.RequestPermissionResult>().toRequestPermissionEffects(),
            filterIsInstance<Result.ErrorRecordingResult>().toErrorRecordingEffects(),
            filterIsInstance<Result.StopRecordingResult>().toPromptSaveFileEffects(),
            filterIsInstance<Result.EffectResult>().toResultEffects()
        )
    }

    private fun Flow<Result.RequestPermissionResult>.toRequestPermissionEffects(): Flow<Effect> {
        return mapLatest { Effect.RequestPermissionEffect(permission = audioRepository.permission()) }
    }

    private fun Flow<Result.ErrorRecordingResult>.toErrorRecordingEffects(): Flow<Effect> {
        return mapLatest { result -> Effect.ErrorRecordingEffect(result.throwable) }
    }

    private fun Flow<Result.StopRecordingResult>.toPromptSaveFileEffects(): Flow<Effect> {
        return mapLatest { result -> Effect.PromptSaveFileEffect(result.cachedFilename) }
    }

    private fun Flow<Result.EffectResult>.toResultEffects(): Flow<Effect> {
        return mapLatest { result -> result.effect }
    }

    class Factory @Inject constructor(
        private val audioRepository: AudioRepository
    ) {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(CachedFilenameHandle(handle), audioRepository) as T
                }
            }
        }
    }
}
