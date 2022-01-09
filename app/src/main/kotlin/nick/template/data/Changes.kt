package nick.template.data

sealed class Event {
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
        object Stop : RecordEvent()
    }
    data class SaveRecordingEvent(val filename: String, val copyToMusicFolder: Boolean) : Event()
    object CancelSaveRecordingEvent : Event()
    object OpenAppSettingsEvent : Event()
}

sealed class Result {
    sealed class RequestPermissionResult : Result() {
        object FromCreation : RequestPermissionResult()
        object FromStartRecording : RequestPermissionResult()
    }
    data class StartRecordingResult(val cachedFilename: CachedFilename) : Result()
    data class ErrorRecordingResult(val throwable: Throwable) : Result()
    data class StopRecordingResult(val cachedFilename: CachedFilename) : Result()
    object NoOpResult : Result()
    object CachedRecordingClearedResult : Result()
    data class EffectResult(val effect: Effect) : Result()
}

sealed class Effect {
    data class ErrorRecordingEffect(val throwable: Throwable) : Effect()
    data class PromptSaveFileEffect(val cachedFilename: CachedFilename) : Effect()
    data class RequestPermissionEffect(val permission: String) : Effect()
    object StartRecordingEffect : Effect()
    object PermissionRationaleEffect : Effect()
    object TellUserToEnablePermissionFromSettingsEffect : Effect()
    data class OpenAppSettingsEffect(val parts: AppSettingsParts) : Effect()
}

data class State(
    val isRecording: Boolean = false,
    val cachedFilename: CachedFilename? = null,
    val startRecordingAfterPermissionGranted: Boolean = false
)
