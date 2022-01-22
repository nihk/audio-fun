package com.audio.recorder.data

import androidx.lifecycle.SavedStateHandle
import com.audio.files.Filename

// Save this temporary filename to state, so it'll survive across process recreation and enable
// the user to save any recording that had been made before process death.
internal class TempFilenameHandle(private val delegate: SavedStateHandle) {
    var filename: Filename?
        get() = delegate[KEY_CACHED_FILENAME]
        set(value) {
            delegate[KEY_CACHED_FILENAME] = value
        }

    fun require(): Filename {
        return requireNotNull(filename) { "filename was requested but it was null" }
    }

    fun consume(): Filename {
        return require().also { filename = null }
    }

    companion object {
        private const val KEY_CACHED_FILENAME = "cached_filename"
    }
}
