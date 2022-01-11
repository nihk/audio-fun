package nick.template.data

import androidx.lifecycle.SavedStateHandle

class CachedFilenameHandle(private val delegate: SavedStateHandle) {
    var filename: CachedFilename?
        get() = delegate[KEY_CACHED_FILENAME]
        set(value) {
            delegate[KEY_CACHED_FILENAME] = value
        }

    fun require(): CachedFilename {
        return requireNotNull(filename) { "filename was requested but it was null" }
    }

    fun consume(): CachedFilename {
        return require().also { filename = null }
    }

    companion object {
        private const val KEY_CACHED_FILENAME = "cached_filename"
    }
}
