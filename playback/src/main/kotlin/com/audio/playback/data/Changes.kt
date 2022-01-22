package com.audio.playback.data

internal sealed class Event {
    data class CreatePlayerEvent(val start: Boolean) : Event()
    object Play : Event()
    object Pause : Event()
}

internal sealed class Result {
    data class PlayingStateChangedResult(val isPlaying: Boolean) : Result()
    object NoOpResult : Result()
    data class EffectResult(val effect: Effect) : Result()
}

internal sealed class Effect {
    data class ErrorEffect(val what: Int) : Effect()
}

internal data class State(
    val isPlaying: Boolean = false
)
