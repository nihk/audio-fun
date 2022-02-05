package com.audio.playback.ui

import com.audio.core.ui.NavigationDirections

@Suppress("FunctionName")
fun PlaybackDirections(recordingName: String): NavigationDirections {
    return NavigationDirections(
        screen = PlaybackFragment::class.java,
        arguments = PlaybackFragment.bundle(recordingName)
    )
}
