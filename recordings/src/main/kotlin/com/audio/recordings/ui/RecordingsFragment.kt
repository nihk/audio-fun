package com.audio.recordings.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.audio.core.extensions.clicks
import com.audio.recordings.R
import com.audio.recordings.data.Effect
import com.audio.recordings.data.Event
import com.audio.recordings.databinding.RecordingsFragmentBinding
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

class RecordingsFragment @Inject constructor(
    private val factory: RecordingsViewModel.Factory,
    private val recordingsNavigator: RecordingsNavigator
) : Fragment(R.layout.recordings_fragment) {
    private val viewModel: RecordingsViewModel by viewModels { factory.create(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = RecordingsFragmentBinding.bind(view)

        val states = viewModel.states

        val effects = viewModel.effects
            .onEach { effect ->
                when (effect) {
                    Effect.NavigateToRecorderEffect -> recordingsNavigator.toRecorder()
                }
            }

        val events = merge(
            binding.record.clicks().map { Event.RecordEvent }
        ).onEach(viewModel::processEvent)

        merge(states, effects, events).launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
