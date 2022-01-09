package nick.template.data

sealed class Event {
    object RequestPermissionEvent : Event()
    sealed class PermissionResultEvent : Event() {
        object Granted : PermissionResultEvent()
        object Denied : PermissionResultEvent()
    }
    sealed class RecordEvent : Event() {
        object Start : RecordEvent()
        object Stop : RecordEvent()
    }
    data class SaveRecordingEvent(val filename: String, val copyToMusicFolder: Boolean) : Event()
    object CancelSaveRecordingEvent : Event()
}

sealed class Result {
    data class StartRecordingResult(val cachedFilename: CachedFilename) : Result()
    data class ErrorRecordingResult(val throwable: Throwable) : Result()
    data class StopRecordingResult(val cachedFilename: CachedFilename) : Result()
    object NoOpResult : Result()
    object CachedRecordingClearedResult : Result()
}

sealed class Effect {
    data class ErrorRecordingEffect(val throwable: Throwable) : Effect()
    data class PromptSaveFileEffect(val cachedFilename: CachedFilename) : Effect()
    data class RequestPermissionEffect(val permission: String) : Effect()
}

data class State(
    val cachedFilename: CachedFilename? = null,
)
