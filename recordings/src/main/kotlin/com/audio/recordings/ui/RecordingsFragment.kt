package com.audio.recordings.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.audio.core.extensions.clicks
import com.audio.recordings.R
import com.audio.recordings.data.Effect
import com.audio.recordings.data.Event
import com.audio.recordings.databinding.RecordingsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class RecordingsFragment @Inject constructor(
    private val navigator: RecordingsNavigator
) : Fragment(R.layout.recordings_fragment) {
    private val viewModel: RecordingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = RecordingsFragmentBinding.bind(view)
        val adapter = RecordingsAdapter()
        binding.recyclerView.adapter = adapter

        val states = viewModel.states
            .onEach { state -> adapter.submitList(state.recordings) }

        val effects = viewModel.effects
            .onEach { effect ->
                when (effect) {
                    Effect.NavigateToRecorderEffect -> navigator.toRecorder()
                    is Effect.NavigateToPlaybackEffect -> navigator.toPlayback(effect.recordingName)
                }
            }

        val events = merge(
            binding.record.clicks().map { Event.ToRecorderEvent },
            adapter.events(),
            viewLifecycleOwner.lifecycle.events()
        ).onEach(viewModel::processEvent)

        merge(states, effects, events)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun Lifecycle.events(): Flow<Event> = callbackFlow {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> trySend(Event.ShowRecordingsEvent(Event.ShowRecordingsEvent.Action.Start))
                Lifecycle.Event.ON_STOP -> if (!requireActivity().isChangingConfigurations) {
                    trySend(Event.ShowRecordingsEvent(Event.ShowRecordingsEvent.Action.Stop))
                }
            }
        }

        addObserver(observer)

        awaitClose { removeObserver(observer) }
    }
}
