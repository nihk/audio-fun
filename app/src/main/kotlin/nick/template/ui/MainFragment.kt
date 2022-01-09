package nick.template.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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
import nick.template.databinding.MainFragmentBinding
import nick.template.ui.dialogs.PermissionRationaleDialogFragment
import nick.template.ui.dialogs.SaveRecordingDialogFragment
import nick.template.ui.dialogs.TellUserToEnablePermissionViaSettingsDialogFragment
import nick.template.ui.extensions.clicks

// todo: probably should have a foreground service for recording
// todo: don't save to cache, save to app disk space (non-cache) and add an option to copy to Music folder
// todo: pause/resume on API >= 24. this will mess with the way the repository coroutine flow works, tho
class MainFragment @Inject constructor(
    private val factory: MainViewModel.Factory
) : Fragment(R.layout.main_fragment),
    SaveRecordingDialogFragment.Listener,
    PermissionRationaleDialogFragment.Listener,
    TellUserToEnablePermissionViaSettingsDialogFragment.Listener {
    private val viewModel: MainViewModel by viewModels { factory.create(this) }
    private val relay = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private val permissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val (permission, didPermit) = results.entries.single()

            val event = when {
                didPermit -> Event.PermissionResultEvent.Granted
                shouldShowRequestPermissionRationale(permission) -> Event.PermissionResultEvent.ShowRationale
                else -> Event.PermissionResultEvent.Denied
            }
            relay.tryEmit(event)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MainFragmentBinding.bind(view)

        val states = viewModel.states
            .onEach { state ->
                binding.recordingFilename.text = state.cachedFilename?.absolute.orEmpty()
            }

        val effects = viewModel.effects
            .onEach { effect ->
                when (effect) {
                    is Effect.ErrorRecordingEffect -> Log.d("asdf", "error", effect.throwable)
                    is Effect.PromptSaveFileEffect -> {
                        Log.d("asdf", "prompting to save file")
                        SaveRecordingDialogFragment
                            .create(defaultFilename = effect.cachedFilename.simple)
                            .show(childFragmentManager, null)
                    }
                    is Effect.RequestPermissionEffect -> permissions.launch(arrayOf(effect.permission))
                    Effect.StartRecordingEffect -> relay.emit(Event.RecordEvent.Start)
                    Effect.PermissionRationaleEffect -> PermissionRationaleDialogFragment().show(childFragmentManager, null)
                    Effect.TellUserToEnablePermissionFromSettingsEffect -> TellUserToEnablePermissionViaSettingsDialogFragment().show(childFragmentManager, null)
                    is Effect.OpenAppSettingsEffect -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            val (scheme, packageName) = effect.parts
                            data = Uri.fromParts(scheme, packageName, null)
                        }
                        startActivity(intent)
                    }
                }
            }

        val events = merge(
            binding.start.clicks().map { Event.RequestPermissionEvent.FromStartRecording },
            binding.pause.clicks().map { Event.RecordEvent.Pause },
            binding.resume.clicks().map { Event.RecordEvent.Resume },
            binding.stop.clicks().map { Event.RecordEvent.Stop },
            relay
        )
            .onEach(viewModel::processEvent)

        merge(states, effects, events).launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun saveRecordingResult(result: SaveRecordingDialogFragment.Result) {
        val event = when (result) {
            SaveRecordingDialogFragment.Result.Delete -> Event.CancelSaveRecordingEvent
            is SaveRecordingDialogFragment.Result.SaveRecordingRequested -> Event.SaveRecordingEvent(
                filename = result.filename,
                copyToMusicFolder = result.copyToMusicFolder
            )
        }
        relay.tryEmit(event)
    }

    override fun onRationaleExplained() {
        relay.tryEmit(Event.RequestPermissionEvent.General)
    }

    override fun openAppSettings() {
        relay.tryEmit(Event.OpenAppSettingsEvent)
    }
}
