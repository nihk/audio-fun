package com.audio.recordings.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.audio.core.extensions.clicks
import com.audio.recordings.R
import com.audio.recordings.data.Effect
import com.audio.recordings.data.Event
import com.audio.recordings.databinding.RecordingsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class RecordingsFragment @Inject constructor(
    private val recordingsNavigator: RecordingsNavigator
) : Fragment(R.layout.recordings_fragment) {
    private val viewModel: RecordingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = RecordingsFragmentBinding.bind(view)
        val adapter = RecordingsAdapter()
        binding.recyclerView.adapter = adapter

        val states = viewModel.states
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state -> adapter.submitList(state.recordings) }

        val effects = viewModel.effects
            .onEach { effect ->
                when (effect) {
                    Effect.NavigateToRecorderEffect -> recordingsNavigator.toRecorder()
                }
            }

        val events = merge(
            binding.record.clicks().map { Event.ToRecorderEvent },
            adapter.events()
        ).onEach(viewModel::processEvent)

        merge(states, effects, events).launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
