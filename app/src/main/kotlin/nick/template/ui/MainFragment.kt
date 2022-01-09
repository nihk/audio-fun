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
// todo: ask for permission when recording as started
class MainFragment @Inject constructor(
    private val factory: MainViewModel.Factory
) : Fragment(R.layout.main_fragment), SaveRecordingDialogFragment.Listener {
    private val viewModel: MainViewModel by viewModels { factory.create(this) }
    private val permissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { didPermit: Boolean ->
            if (!didPermit) error("Needs record audio permission")
        }
    private val relay = MutableSharedFlow<Event>(extraBufferCapacity = 1)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MainFragmentBinding.bind(view)
        permissions.launch(Manifest.permission.RECORD_AUDIO)

        val states = viewModel.states
            .onEach { state ->
                binding.recordingFilename.text = state.cachedFilename.orEmpty()
            }

        val effects = viewModel.effects
            .onEach { effect ->
                when (effect) {
                    is Effect.ErrorRecordingEffect -> Log.d("asdf", "error", effect.throwable)
                    is Effect.PromptSaveFileEffect -> {
                        Log.d("asdf", "prompting to save file")
                        SaveRecordingDialogFragment().show(childFragmentManager, null)
                    }
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
            SaveRecordingDialogFragment.Result.Cancelled -> Event.CancelSaveRecordingEvent
            is SaveRecordingDialogFragment.Result.SaveRecordingRequested -> Event.SaveRecordingEvent(result.filename)
        }
        relay.tryEmit(event)
    }
}
