package com.audio.playback.data

sealed class Event {
    data class CreatePlayerEvent(val start: Boolean) : Event()
    object Play : Event()
    object Pause : Event()
}

sealed class Result {
    data class PlayingStateChangedResult(val isPlaying: Boolean) : Result()
    object NoOpResult : Result()
}

sealed class Effect {
}

data class State(
    val isPlaying: Boolean = false
)
