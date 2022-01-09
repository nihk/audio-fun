package nick.template.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.databinding.SaveRecordingDialogFragmentBinding
import nick.template.ui.extensions.clicks
import nick.template.ui.extensions.textChanges

// fixme: UI isn't great
class SaveRecordingDialogFragment : DialogFragment(R.layout.save_recording_dialog_fragment) {
    private lateinit var listener: Listener
    private val dialogCancels = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    interface Listener {
        fun saveRecordingResult(result: Result)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as Listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = SaveRecordingDialogFragmentBinding.bind(view)

        val states = binding.input.textChanges()
            .onEach { text -> binding.save.isEnabled = !text.isNullOrBlank() }

        val results = merge(
            merge(binding.cancel.clicks(), dialogCancels).map { Result.Cancelled },
            binding.save.clicks().map {
                val filename = binding.input.text.toString()
                Result.SaveRecordingRequested(filename)
            },
        )
            .onEach { result ->
                listener.saveRecordingResult(result)
                dismiss()
            }

        merge(states, results).launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onCancel(dialog: DialogInterface) {
        dialogCancels.tryEmit(Unit)
    }

    sealed class Result {
        data class SaveRecordingRequested(val filename: String) : Result()
        object Cancelled : Result()
    }
}
