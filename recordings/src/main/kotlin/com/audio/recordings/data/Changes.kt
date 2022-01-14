package com.audio.recordings.data

sealed class Event {
    object RecordEvent : Event()
}

sealed class Result {
    data class EffectResult(val effect: Effect) : Result()
}

sealed class Effect {
    object NavigateToRecorderEffect : Effect()
}

data class State(
    val items: List<Any> = emptyList()
)
