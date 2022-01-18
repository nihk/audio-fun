package com.audio.playback.data

sealed class Event {
    object ListenToPlayerEvent : Event()
    data class CreatePlayerEvent(val start: Boolean) : Event()
}

sealed class Result {
    data class EffectResult(val effect: Effect) : Result()
    object NoOpResult : Result()
}

sealed class Effect {
    object ListeningToPlayerEffect : Effect()
}

data class State(
    val any: Any? = null
)
