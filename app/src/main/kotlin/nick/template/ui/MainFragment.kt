package nick.template.ui

import android.Manifest
import android.os.Bundle
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
import nick.template.ui.extensions.clicks

// todo: probably should have a foreground service for recording
// todo: ask/check for permission when request to record is made
// todo: don't save to cache, save to app disk space (non-cache) and add an option to copy to Music folder
class MainFragment @Inject constructor(
    private val factory: MainViewModel.Factory
) : Fragment(R.layout.main_fragment), SaveRecordingDialogFragment.Listener {
    private val viewModel: MainViewModel by viewModels { factory.create(this) }
    private val permissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { didPermit: Boolean ->
            val event = if (didPermit) Event.PermissionResultEvent.Granted else Event.PermissionResultEvent.Denied
            relay.tryEmit(event)
        }
    private val relay = MutableSharedFlow<Event>(extraBufferCapacity = 1)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MainFragmentBinding.bind(view)
        permissions.launch(Manifest.permission.RECORD_AUDIO)

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
                    is Effect.RequestPermissionEffect -> permissions.launch(effect.permission)
                }
            }

        val events = merge(
            binding.start.clicks().map { Event.RecordEvent.Start },
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
}
