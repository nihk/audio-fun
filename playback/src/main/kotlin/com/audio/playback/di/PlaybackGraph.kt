package com.audio.playback.di

import androidx.fragment.app.Fragment
import com.audio.core.di.CoreGraph
import com.audio.playback.data.MediaPlayerPlaybackRepository
import com.audio.playback.ui.PlaybackFragment
import com.audio.playback.ui.PlaybackViewModel

class PlaybackGraph(private val coreGraph: CoreGraph) {
    val playbackFragment: Pair<Class<out Fragment>, () -> Fragment> get() {
        return PlaybackFragment::class.java to {
            PlaybackFragment(
                viewModelFactory = { owner, recordingName ->
                    PlaybackViewModel.Factory(
                        repository = MediaPlayerPlaybackRepository(
                            context = coreGraph.appContext
                        ),
                        recordingName = recordingName
                    ).create(owner)
                }
            )
        }
    }
}
