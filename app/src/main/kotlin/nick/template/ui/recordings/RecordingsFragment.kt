package nick.template.ui.recordings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.databinding.RecordingsFragmentBinding
import nick.template.ui.extensions.clicks

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
