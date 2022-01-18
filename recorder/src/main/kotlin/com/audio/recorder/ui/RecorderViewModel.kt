package com.audio.recorder.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.audio.core.mvi.MviViewModel
import com.audio.recorder.data.AudioPermissionsRepository
import com.audio.recorder.data.AudioRepository
import com.audio.recorder.data.TempFilenameHandle
import com.audio.recorder.data.Effect
import com.audio.recorder.data.Event
import com.audio.recorder.data.Result
import com.audio.recorder.data.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transformLatest

// todo: need to clean up audioRepository when VM is cleared?
@HiltViewModel
class RecorderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val audioRepository: AudioRepository,
    private val permissionsRepository: AudioPermissionsRepository
) : MviViewModel<Event, Result, State, Effect>(State()) {
    private val handle = TempFilenameHandle(savedStateHandle)

    override suspend fun onSubscription() {
        processEvent(Event.RequestPermissionEvent.General)
        processEvent(Event.ListenToMediaRecording)
    }

    override fun Result.reduce(state: State): State {
        return when (this) {
            is Result.StartRecordingResult -> state.copy(
                recording = State.Recording.Recording,
                tempFilename = tempFilename,
                startRecordingAfterPermissionGranted = false
            )
            Result.PauseRecordingResult -> state.copy(recording = State.Recording.Paused)
            Result.ResumeRecordingResult -> state.copy(recording = State.Recording.Recording)
            is Result.StopRecordingResult -> state.copy(recording = State.Recording.Stopped)
            is Result.FinishedRecordingResult -> state.copy(tempFilename = null, amplitudes = emptyList())
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
                        handle.filename = emission.tempFilename
                        Result.StartRecordingResult(emission.tempFilename)
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
        return transformLatest { event ->
            when (event) {
                Event.PermissionResultEvent.Granted -> if (states.value.startRecordingAfterPermissionGranted) {
                    Log.d("asdf", "permission granted; starting recording")
                    emit(Result.EffectResult(Effect.StartRecordingEffect))
                }
                Event.PermissionResultEvent.ShowRationale -> {
                    Log.d("asdf", "explaining need for permission to user")
                    emit(Result.EffectResult(Effect.PermissionRationaleEffect))
                }
                Event.PermissionResultEvent.Denied -> {
                    Log.d("asdf", "permission denied, prompting user to enable it via settings")
                    emit(Result.EffectResult(Effect.TellUserToEnablePermissionFromSettingsEffect))
                }
            }
        }
    }

    private fun Flow<Event.RecordEvent>.toRecordingResults(): Flow<Result> {
        return transformLatest { event ->
            when (event) {
                Event.RecordEvent.Start -> audioRepository.start()
                Event.RecordEvent.Pause -> audioRepository.pause()
                Event.RecordEvent.Resume -> audioRepository.resume()
                Event.RecordEvent.Stop -> audioRepository.stop()
            }
        }
    }

    private fun Flow<Event.SaveRecordingEvent>.toSaveRecordingResults(): Flow<Result> {
        return mapLatest { event ->
            val tempFilename = handle.consume()
            audioRepository.save(
                tempFilename = tempFilename,
                newName = event.filename,
                copyToMusicFolder = event.copyToMusicFolder
            )
            Result.FinishedRecordingResult
        }
    }

    private fun Flow<Event.DeleteSaveRecordingEvent>.toDeleteSaveRecordingResults(): Flow<Result> {
        return mapLatest {
            audioRepository.cleanup(handle.consume())
            Result.FinishedRecordingResult
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
            filterIsInstance<Result.EffectResult>().toResultEffects(),
            filterIsInstance<Result.FinishedRecordingResult>().toFinishedRecordngEffects()
        )
    }

    private fun Flow<Result.RequestPermissionResult>.toRequestPermissionEffects(): Flow<Effect> {
        return mapLatest { Effect.RequestPermissionEffect(permission = permissionsRepository.permission()) }
    }

    private fun Flow<Result.ErrorRecordingResult>.toErrorRecordingEffects(): Flow<Effect> {
        return mapLatest { result -> Effect.ErrorRecordingEffect(result.throwable) }
    }

    private fun Flow<Result.StopRecordingResult>.toPromptSaveFileEffects(): Flow<Effect> {
        return mapLatest { result -> Effect.PromptSaveFileEffect(result.tempFilename) }
    }

    private fun Flow<Result.EffectResult>.toResultEffects(): Flow<Effect> {
        return mapLatest { result -> result.effect }
    }

    private fun Flow<Result.FinishedRecordingResult>.toFinishedRecordngEffects(): Flow<Effect> {
        return mapLatest { Effect.FinishedRecordingEffect }
    }
}
