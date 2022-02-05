package com.audio.playback.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistryOwner
import com.audio.core.extensions.clicks
import com.audio.playback.R
import com.audio.playback.data.Effect
import com.audio.playback.data.Event
import com.audio.playback.databinding.PlaybackFragmentBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

internal class PlaybackFragment(
    private val viewModelFactory: (SavedStateRegistryOwner, String) -> ViewModelProvider.Factory
) : Fragment(R.layout.playback_fragment) {
    private val recordingName: String get() = requireArguments().getString(KEY_RECORDING_NAME)!!
    private val viewModel: PlaybackViewModel by viewModels { viewModelFactory(this, recordingName) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = PlaybackFragmentBinding.bind(view)

        val states = viewModel.states
            .onEach { state ->
                val (text, tag) = if (state.isPlaying) {
                    R.string.pause to true
                } else {
                    R.string.play to false
                }
                binding.playPause.setText(text)
                binding.playPause.tag = tag
            }

        val effects = viewModel.effects
            .onEach { effect ->
                when (effect) {
                    is Effect.ErrorEffect -> Toast.makeText(view.context, "Error: ${effect.what}", Toast.LENGTH_LONG).show()
                }
            }

        val events = merge(
            binding.playPause.clicks().map {
                if (binding.playPause.tag as Boolean) {
                    Event.Pause
                } else {
                    Event.Play
                }
            },
        )
            .onEach(viewModel::processEvent)

        merge(states, effects, events).launchIn(viewLifecycleOwner.lifecycleScope)
    }

    companion object {
        private const val KEY_RECORDING_NAME = "recording_name"

        fun bundle(recordingName: String): Bundle {
            return bundleOf(KEY_RECORDING_NAME to recordingName)
        }
    }
}
