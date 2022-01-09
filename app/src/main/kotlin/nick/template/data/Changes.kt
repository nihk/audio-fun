package nick.template.data

sealed class Event {
    data class RecordEvent(val start: Boolean) : Event()
    data class SaveRecordingEvent(val filename: String) : Event()
    object CancelSaveRecordingEvent : Event()
}

sealed class Result {
    data class StartRecordingResult(val cachedFilename: String) : Result()
    data class ErrorRecordingResult(val throwable: Throwable) : Result()
    object StopRecordingResult : Result()
    object NoOpResult : Result()
    object CachedRecordingClearedResult : Result()
}

sealed class Effect {
    data class ErrorRecordingEffect(val throwable: Throwable) : Effect()
    object PromptSaveFileEffect : Effect()
}

data class State(
    val cachedFilename: String? = null,
)
