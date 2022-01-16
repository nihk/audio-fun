package com.audio.recordings.data

sealed class Event {
    object ShowRecordingsEvent : Event()
    data class DeleteRecordingEvent(val recording: Recording) : Event()
    object ToRecorderEvent : Event()
}

sealed class Result {
    data class ShowRecordingsResult(val recordings: List<Recording>) : Result()
    data class EffectResult(val effect: Effect) : Result()
}

sealed class Effect {
    object NavigateToRecorderEffect : Effect()
}

data class State(
    val recordings: List<Recording>? = null
)
