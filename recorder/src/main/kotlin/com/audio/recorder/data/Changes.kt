package com.audio.recorder.data

import com.audio.files.Filename

internal sealed class Event {
    object ListenToMediaRecording : Event()
    sealed class RequestPermissionEvent : Event() {
        object General : RequestPermissionEvent()
        object FromStartRecording : RequestPermissionEvent()
    }
    sealed class PermissionResultEvent : Event() {
        object Granted : PermissionResultEvent()
        object ShowRationale : PermissionResultEvent()
        object Denied : PermissionResultEvent()
    }
    sealed class RecordEvent : Event() {
        object Start : RecordEvent()
        object Pause : RecordEvent()
        object Resume : RecordEvent()
        object Stop : RecordEvent()
    }
    data class SaveRecordingEvent(val filename: String, val copyToMusicFolder: Boolean) : Event()
    object DeleteSaveRecordingEvent : Event()
    object OpenAppSettingsEvent : Event()
    object BackPressWhileRecordingEvent : Event()
}

internal sealed class Result {
    sealed class RequestPermissionResult : Result() {
        object General : RequestPermissionResult()
        object FromStartRecording : RequestPermissionResult()
    }
    data class StartRecordingResult(val tempFilename: Filename) : Result()
    data class ErrorRecordingResult(val throwable: Throwable) : Result()
    object PauseRecordingResult : Result()
    object ResumeRecordingResult : Result()
    data class StopRecordingResult(val tempFilename: Filename) : Result()
    object FinishedRecordingResult : Result()
    data class EffectResult(val effect: Effect) : Result()
    data class AmplitudesResult(val amplitudes: List<Int>) : Result()
}

internal sealed class Effect {
    data class ErrorRecordingEffect(val throwable: Throwable) : Effect()
    data class PromptSaveFileEffect(val tempFilename: Filename) : Effect()
    data class RequestPermissionEffect(val permission: String) : Effect()
    object StartRecordingEffect : Effect()
    object PermissionRationaleEffect : Effect()
    object TellUserToEnablePermissionFromSettingsEffect : Effect()
    data class OpenAppSettingsEffect(val parts: AppSettingsParts) : Effect()
    object ConfirmStopRecordingEffect : Effect()
    object FinishedRecordingEffect : Effect()
}

internal data class State(
    val recording: Recording = Recording.Stopped,
    val isPaused: Boolean = false,
    val tempFilename: Filename? = null,
    val startRecordingAfterPermissionGranted: Boolean = false,
    val amplitudes: List<Int> = emptyList()
) {
    enum class Recording {
        Recording, Paused, Stopped
    }
}
