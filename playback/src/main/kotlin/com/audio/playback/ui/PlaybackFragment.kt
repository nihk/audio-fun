package com.audio.playback.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.audio.playback.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge

@AndroidEntryPoint
class PlaybackFragment @Inject constructor() : Fragment(R.layout.playback_fragment) {
    private val viewModel: PlaybackViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val states = viewModel.states
        val effects = viewModel.effects

        merge(states, effects).launchIn(viewLifecycleOwner.lifecycleScope)
    }

    companion object {
        internal const val KEY_RECORDING_NAME = "recording_name"

        fun bundle(recordingName: String): Bundle {
            return bundleOf(KEY_RECORDING_NAME to recordingName)
        }
    }
}
