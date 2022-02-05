package com.audio.app.di

import androidx.fragment.app.FragmentManager
import com.audio.R
import com.audio.app.ui.navigation.FragmentRecordingsNavigator
import com.audio.core.ui.AppFragmentFactory
import com.audio.playback.di.PlaybackGraph
import com.audio.recorder.di.RecorderGraph
import com.audio.recordings.di.RecordingsGraph

class MainGraph(
    appGraph: AppGraph,
    fragmentManager: FragmentManager
) {
    private val recordingsGraph = RecordingsGraph(
        navigator = FragmentRecordingsNavigator(
            fragmentManager = fragmentManager,
            containerId = R.id.fragment_container
        ),
        coreGraph = appGraph.coreGraph
    )

    private val recorderGraph = RecorderGraph(
        coreGraph = appGraph.coreGraph
    )

    private val playbackGraph = PlaybackGraph(
        coreGraph = appGraph.coreGraph
    )

    val fragmentFactory = AppFragmentFactory(
        fragments = mapOf(
            recordingsGraph.recordingsFragment,
            recorderGraph.recorderFragment,
            playbackGraph.playbackFragment
        )
    )
}
