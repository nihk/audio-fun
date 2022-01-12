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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import nick.template.data.AudioPermissionsRepository
import nick.template.data.AudioRepository
import nick.template.data.CachedFilenameHandle
import nick.template.data.Effect
import nick.template.data.Event
import nick.template.data.Result
import nick.template.data.State

// todo: need to clean up audioRepository when VM is cleared?
class RecorderViewModel(
    private val handle: CachedFilenameHandle,
    private val audioRepository: AudioRepository,
    private val permissionsRepository: AudioPermissionsRepository
) : MviViewModel<Event, Result, State, Effect>(State()) {

    override fun onStart() {
        processEvent(Event.RequestPermissionEvent.General)
        processEvent(Event.ListenToMediaRecording)
    }

    override fun Result.reduce(state: State): State {
        return when (this) {
            is Result.StartRecordingResult -> state.copy(
                recording = State.Recording.Recording,
                cachedFilename = cachedFilename,
                startRecordingAfterPermissionGranted = false
            )
            Result.PauseRecordingResult -> state.copy(recording = State.Recording.Paused)
            Result.ResumeRecordingResult -> state.copy(recording = State.Recording.Recording)
            is Result.StopRecordingResult -> state.copy(recording = State.Recording.Stopped)
            is Result.CachedRecordingDeletedResult -> state.copy(cachedFilename = null, amplitudes = emptyList())
            is Result.RequestPermissionResult.FromStartRecording -> state.copy(startRecordingAfterPermissionGranted = true)
            is Result.AmplitudeResult -> state.copy(amplitudes = state.amplitudes + amplitude)
            else -> state
        }
    }

    override fun Flow<Event>.toResults(): Flow<Result> {
        return merge(
            filterIsInstance<Event.ListenToMediaRecording>().toMediaRecordingResults(),
            filterIsInstance<Event.RequestPermissionEvent>().toRequestPermissionResults(),
            filterIsInstance<Event.PermissionResultEvent>().toPermissionResults(),
            filterIsInstance<Event.RecordEvent>().toRecordingResults(),
            filterIsInstance<Event.SaveRecordingEvent>().toSaveRecordingResults(),
            filterIsInstance<Event.DeleteSaveRecordingEvent>().toDeleteSaveRecordingResults(),
            filterIsInstance<Event.OpenAppSettingsEvent>().toOpenAppSettingsResults(),
            filterIsInstance<Event.BackPressWhileRecordingEvent>().toBackPressWhileRecordingResults()
        )
    }

    private fun Flow<Event.ListenToMediaRecording>.toMediaRecordingResults(): Flow<Result> {
        return flatMapLatest { audioRepository.emissions() }
            .map { emission ->
                when (emission) {
                    is AudioRepository.Emission.StartedRecording -> {
                        handle.filename = emission.cachedFilename
                        Result.StartRecordingResult(emission.cachedFilename)
                    }
                    is AudioRepository.Emission.Amplitude -> {
                        Log.d("asdf", "amplitude: ${emission.value}")
                        Result.AmplitudeResult(emission.value)
                    }
                    is AudioRepository.Emission.Error -> Result.ErrorRecordingResult(emission.throwable)
                    AudioRepository.Emission.PausedRecording -> Result.PauseRecordingResult
                    AudioRepository.Emission.ResumedRecording -> Result.ResumeRecordingResult
                    AudioRepository.Emission.FinishedRecording -> Result.StopRecordingResult(handle.require())
                }
            }
    }

    private fun Flow<Event.RequestPermissionEvent>.toRequestPermissionResults(): Flow<Result> {
        return mapLatest { event ->
            when (event) {
                Event.RequestPermissionEvent.General -> Result.RequestPermissionResult.General
                Event.RequestPermissionEvent.FromStartRecording -> Result.RequestPermissionResult.FromStartRecording
            }
        }
    }

    private fun Flow<Event.PermissionResultEvent>.toPermissionResults(): Flow<Result> {
        return mapLatest { event ->
            when (event) {
                Event.PermissionResultEvent.Granted -> if (states.value.startRecordingAfterPermissionGranted) {
                    Log.d("asdf", "permission granted; starting recording")
                    Result.EffectResult(Effect.StartRecordingEffect)
                } else {
                    Result.NoOpResult // Granted permission from the initial screen prompt
                }
                Event.PermissionResultEvent.ShowRationale -> {
                    Log.d("asdf", "explaining need for permission to user")
                    Result.EffectResult(Effect.PermissionRationaleEffect)
                }
                Event.PermissionResultEvent.Denied -> {
                    Log.d("asdf", "permission denied, prompting user to enable it via settings")
                    Result.EffectResult(Effect.TellUserToEnablePermissionFromSettingsEffect)
                }
            }
        }
    }

    private fun Flow<Event.RecordEvent>.toRecordingResults(): Flow<Result> {
        return mapLatest { event ->
            when (event) {
                Event.RecordEvent.Start -> audioRepository.start()
                Event.RecordEvent.Pause -> audioRepository.pause()
                Event.RecordEvent.Resume -> audioRepository.resume()
                Event.RecordEvent.Stop -> audioRepository.stop()
            }

            Result.NoOpResult // Results are handled during repository emissions
        }
    }

    private fun Flow<Event.SaveRecordingEvent>.toSaveRecordingResults(): Flow<Result> {
        return mapLatest { event ->
            val cachedFilename = handle.consume()
            audioRepository.save(
                cachedFilename = cachedFilename,
                destinationFilename = event.filename,
                copyToMusicFolder = event.copyToMusicFolder,
                cleanupCache = true
            )
            Result.CachedRecordingDeletedResult
        }
    }

    private fun Flow<Event.DeleteSaveRecordingEvent>.toDeleteSaveRecordingResults(): Flow<Result> {
        return mapLatest {
            audioRepository.deleteFromCache(handle.consume())
            Result.CachedRecordingDeletedResult
        }
    }

    private fun Flow<Event.OpenAppSettingsEvent>.toOpenAppSettingsResults(): Flow<Result> {
        return mapLatest {
            val effect = Effect.OpenAppSettingsEffect(parts = permissionsRepository.appSettingsParts())
            Result.EffectResult(effect)
        }
    }

    private fun Flow<Event.BackPressWhileRecordingEvent>.toBackPressWhileRecordingResults(): Flow<Result> {
        return mapLatest { Result.EffectResult(Effect.ConfirmStopRecordingEffect) }
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
        return mapLatest { Effect.RequestPermissionEffect(permission = permissionsRepository.permission()) }
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
        private val audioRepository: AudioRepository,
        private val permissionsRepository: AudioPermissionsRepository
    ) {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return RecorderViewModel(
                        handle = CachedFilenameHandle(handle),
                        audioRepository = audioRepository,
                        permissionsRepository = permissionsRepository
                    ) as T
                }
            }
        }
    }
}
