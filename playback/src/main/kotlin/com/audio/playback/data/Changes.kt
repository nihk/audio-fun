package com.audio.playback.data

sealed class Event {
    data class StartPlaybackEvent(val recordingName: String) : Event()
}

sealed class Result {

}

sealed class Effect {

}

data class State(
    val any: Any? = null
)
