package com.audio.recorder.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.audio.core.di.entryPoint
import com.audio.core.extensions.clicks
import com.audio.recorder.R
import com.audio.recorder.data.Effect
import com.audio.recorder.data.Event
import com.audio.recorder.data.State
import com.audio.recorder.databinding.RecorderFragmentBinding
import com.audio.recorder.di.RecorderEntryPoint
import com.audio.recorder.dialogs.ConfirmStopRecordingDialogFragment
import com.audio.recorder.dialogs.PermissionRationaleDialogFragment
import com.audio.recorder.dialogs.SaveRecordingDialogFragment
import com.audio.recorder.dialogs.TellUserToEnablePermissionViaSettingsDialogFragment
import com.audio.recorder.extensions.add
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

// todo: probably should have a foreground service for recording
@AndroidEntryPoint
class RecorderFragment @Inject constructor() : Fragment(R.layout.recorder_fragment) {
    private val viewModel: RecorderViewModel by viewModels()
    private val relay = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val (permission, didPermit) = results.entries.single()
        val event = when {
            didPermit -> Event.PermissionResultEvent.Granted
            shouldShowRequestPermissionRationale(permission) -> Event.PermissionResultEvent.ShowRationale
            else -> Event.PermissionResultEvent.Denied
        }
        relay.tryEmit(event)
    }
    private val backPressWhileRecording = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            relay.tryEmit(Event.BackPressWhileRecordingEvent)
        }
    }

    override fun onAttach(context: Context) {
        childFragmentManager.fragmentFactory = entryPoint<RecorderEntryPoint>().fragmentFactory
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = RecorderFragmentBinding.bind(view)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressWhileRecording)

        val states = viewModel.states
            .onEach { state ->
                binding.recordingFilename.text = state.tempFilename?.absolute.orEmpty()
                binding.start.isVisible = state.recording == State.Recording.Stopped
                binding.pause.isVisible = state.recording == State.Recording.Recording
                binding.resume.isVisible = state.recording == State.Recording.Paused
                binding.stop.isVisible = state.recording != State.Recording.Stopped
                backPressWhileRecording.isEnabled = state.recording != State.Recording.Stopped
            }

        val effects = viewModel.effects
            .onEach { effect ->
                when (effect) {
                    is Effect.ErrorRecordingEffect -> Log.d("asdf", "error", effect.throwable)
                    is Effect.PromptSaveFileEffect -> {
                        Log.d("asdf", "prompting to save file")
                        childFragmentManager.commit {
                            add<SaveRecordingDialogFragment>(args = SaveRecordingDialogFragment.bundle(effect.tempFilename.simple))
                        }
                    }
                    is Effect.RequestPermissionEffect -> permissions.launch(arrayOf(effect.permission))
                    Effect.StartRecordingEffect -> relay.emit(Event.RecordEvent.Start)
                    Effect.PermissionRationaleEffect -> childFragmentManager.commit {
                        add<PermissionRationaleDialogFragment>()
                    }
                    Effect.TellUserToEnablePermissionFromSettingsEffect -> childFragmentManager.commit {
                        add<TellUserToEnablePermissionViaSettingsDialogFragment>()
                    }
                    is Effect.OpenAppSettingsEffect -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts(effect.parts.scheme, effect.parts.packageName, null))
                        startActivity(intent)
                    }
                    Effect.ConfirmStopRecordingEffect -> childFragmentManager.commit {
                        add<ConfirmStopRecordingDialogFragment>()
                    }
                    Effect.FinishedRecordingEffect -> requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }

        val events = merge(
            binding.start.clicks().map { Event.RequestPermissionEvent.FromStartRecording },
            binding.pause.clicks().map { Event.RecordEvent.Pause },
            binding.resume.clicks().map { Event.RecordEvent.Resume },
            binding.stop.clicks().map { Event.RecordEvent.Stop },
            relay,
            entryPoint<RecorderEntryPoint>().externalEvents.events(),
        ).onEach(viewModel::processEvent)

        merge(states, effects, events).launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
