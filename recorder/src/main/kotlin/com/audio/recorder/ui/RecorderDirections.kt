package com.audio.recorder.ui

import com.audio.core.ui.NavigationDirections

@Suppress("FunctionName")
fun RecorderDirections(): NavigationDirections {
    return NavigationDirections(
        screen = RecorderFragment::class.java,
        arguments = null
    )
}
