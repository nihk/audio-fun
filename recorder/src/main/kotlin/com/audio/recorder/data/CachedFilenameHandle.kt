package com.audio.recorder.data

import androidx.lifecycle.SavedStateHandle
import com.audio.files.Filename

class CachedFilenameHandle(private val delegate: SavedStateHandle) {
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
