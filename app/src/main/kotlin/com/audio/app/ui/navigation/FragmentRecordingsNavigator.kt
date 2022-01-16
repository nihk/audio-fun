package com.audio.app.ui.navigation

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.audio.app.di.FragmentContainerId
import com.audio.playback.ui.PlaybackFragment
import com.audio.recorder.ui.RecorderFragment
import com.audio.recordings.ui.RecordingsNavigator
import javax.inject.Inject

class FragmentRecordingsNavigator @Inject constructor(
    private val fragmentManager: FragmentManager,
    @FragmentContainerId private val containerId: Int
) : RecordingsNavigator {
    override fun toRecorder() {
        fragmentManager.commit {
            setReorderingAllowed(true)
            replace<RecorderFragment>(containerId)
            addToBackStack(null)
        }
    }

    override fun toPlayback(recordingName: String) {
        fragmentManager.commit {
            setReorderingAllowed(true)
            replace<PlaybackFragment>(containerId)
            addToBackStack(null)
        }
    }
}
