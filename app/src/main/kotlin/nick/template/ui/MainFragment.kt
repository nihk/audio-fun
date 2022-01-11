package nick.template.ui

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
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.data.Effect
import nick.template.data.Event
import nick.template.data.State
import nick.template.databinding.MainFragmentBinding
import nick.template.ui.dialogs.ConfirmStopRecordingDialogFragment
import nick.template.ui.dialogs.ExternalEvents
import nick.template.ui.dialogs.PermissionRationaleDialogFragment
import nick.template.ui.dialogs.SaveRecordingDialogFragment
import nick.template.ui.dialogs.TellUserToEnablePermissionViaSettingsDialogFragment
import nick.template.ui.extensions.clicks

// todo: probably should have a foreground service for recording
// todo: don't save to cache, save to app disk space (non-cache) and add an option to copy to Music folder
// todo: rename to RecorderFragment; add a RecordingsFragment which is a list of all recordings; add PlayerFragment using MediaPlayer
class MainFragment @Inject constructor(
    private val factory: MainViewModel.Factory,
    private val externalEvents: ExternalEvents
) : Fragment(R.layout.main_fragment) {

    private val viewModel: MainViewModel by viewModels { factory.create(this) }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MainFragmentBinding.bind(view)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressWhileRecording)

        val states = viewModel.states
            .onEach { state ->
                binding.recordingFilename.text = state.cachedFilename?.absolute.orEmpty()
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
                            add(SaveRecordingDialogFragment::class.java, SaveRecordingDialogFragment.bundle(effect.cachedFilename.simple), null)
                        }
                    }
                    is Effect.RequestPermissionEffect -> permissions.launch(arrayOf(effect.permission))
                    Effect.StartRecordingEffect -> relay.emit(Event.RecordEvent.Start)
                    Effect.PermissionRationaleEffect -> childFragmentManager.commit {
                        add(PermissionRationaleDialogFragment::class.java, null, null)
                    }
                    Effect.TellUserToEnablePermissionFromSettingsEffect -> childFragmentManager.commit {
                        add(TellUserToEnablePermissionViaSettingsDialogFragment::class.java, null, null)
                    }
                    is Effect.OpenAppSettingsEffect -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts(effect.parts.scheme, effect.parts.packageName, null)
                        }
                        startActivity(intent)
                    }
                    Effect.ConfirmStopRecordingEffect -> childFragmentManager.commit {
                        add(ConfirmStopRecordingDialogFragment::class.java, null, null)
                    }
                }
            }

        val events = merge(
            binding.start.clicks().map { Event.RequestPermissionEvent.FromStartRecording },
            binding.pause.clicks().map { Event.RecordEvent.Pause },
            binding.resume.clicks().map { Event.RecordEvent.Resume },
            binding.stop.clicks().map { Event.RecordEvent.Stop },
            relay,
            externalEvents.events(),
        ).onEach(viewModel::processEvent)

        merge(states, effects, events).launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
