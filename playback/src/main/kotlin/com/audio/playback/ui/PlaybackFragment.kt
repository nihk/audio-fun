package com.audio.playback.ui

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.audio.core.extensions.requireNotNull
import com.audio.playback.R
import javax.inject.Inject

// todo: use MediaPlayer APIs -- not ExoPlayer
class PlaybackFragment @Inject constructor() : Fragment(R.layout.playback_fragment) {
    private val recordingName get() = requireArguments().getString(KEY_RECORDING_NAME).requireNotNull()

    companion object {
        private const val KEY_RECORDING_NAME = "recording_name"

        fun bundle(recordingName: String): Bundle {
            return bundleOf(KEY_RECORDING_NAME to recordingName)
        }
    }
}
