package com.audio.app.ui.navigation

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.audio.recordings.ui.RecordingsNavigator
import javax.inject.Inject
import com.audio.recorder.ui.RecorderFragment

class FragmentRecordingsNavigator @Inject constructor(
    private val fragmentManager: FragmentManager,
    @com.audio.app.di.FragmentContainerId private val containerId: Int
) : RecordingsNavigator {
    override fun toRecorder() {
        fragmentManager.commit {
            setReorderingAllowed(true)
            replace<RecorderFragment>(containerId)
            addToBackStack(null)
        }
    }
}
