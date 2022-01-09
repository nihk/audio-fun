package nick.template.data

import androidx.lifecycle.SavedStateHandle

class CachedFileHandle(private val delegate: SavedStateHandle) {
    var filename: String?
        get() = delegate[KEY_CACHED_FILENAME]
        set(value) {
            delegate[KEY_CACHED_FILENAME] = value
        }

    fun requireFilename() = requireNotNull(filename) {
        "filename was requested but it was null"
    }

    companion object {
        private const val KEY_CACHED_FILENAME = "cached_filename"
    }
}
