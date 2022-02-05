package com.audio.app.ui.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.audio.playback.ui.PlaybackDirections
import com.audio.recorder.ui.RecorderDirections
import com.audio.recordings.ui.RecordingsNavigator

class FragmentRecordingsNavigator(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
) : RecordingsNavigator {
    override fun toRecorder() {
        fragmentManager.commit {
            setReorderingAllowed(true)
            val directions = RecorderDirections()
            replace(containerId, directions.screen, directions.arguments)
            addToBackStack(null)
        }
    }

    override fun toPlayback(recordingName: String) {
        fragmentManager.commit {
            setReorderingAllowed(true)
            val directions = PlaybackDirections(recordingName)
            replace(containerId, directions.screen, directions.arguments)
            addToBackStack(null)
        }
    }
}
