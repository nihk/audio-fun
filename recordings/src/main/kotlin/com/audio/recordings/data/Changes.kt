package com.audio.recordings.data

internal sealed class Event {
    data class ShowRecordingsEvent(val action: Action) : Event() {
        enum class Action {
            Start, Stop
        }
    }
    data class DeleteRecordingEvent(val recording: Recording) : Event()
    data class ToPlaybackEvent(val recording: Recording) : Event()
    object ToRecorderEvent : Event()
}

internal sealed class Result {
    data class ShowRecordingsResult(val recordings: List<Recording>) : Result()
    data class EffectResult(val effect: Effect) : Result()
}

internal sealed class Effect {
    object NavigateToRecorderEffect : Effect()
    data class NavigateToPlaybackEffect(val recordingName: String) : Effect()
}

internal data class State(
    val recordings: List<Recording>? = null
)
