package nick.template.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.databinding.SaveRecordingDialogFragmentBinding
import nick.template.ui.extensions.clicks
import nick.template.ui.extensions.focusAndShowKeyboard
import nick.template.ui.extensions.textChanges

// fixme: UI isn't wide enough
class SaveRecordingDialogFragment : DialogFragment(R.layout.save_recording_dialog_fragment) {
    private lateinit var listener: Listener

    interface Listener {
        fun saveRecordingResult(result: Result)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as Listener
        isCancelable = false // Prevent accidental taps outside the dialog deleting the file
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val defaultFilename = requireArguments().getString(KEY_DEFAULT_FILENAME)
        val binding = SaveRecordingDialogFragmentBinding.bind(view)
        binding.input.setText(defaultFilename)
        binding.input.focusAndShowKeyboard()

        val states = binding.input.textChanges()
            .onEach { text -> binding.save.isEnabled = !text.isNullOrBlank() }

        val results = merge(
            binding.delete.clicks().map { Result.Delete },
            binding.save.clicks().map {
                val filename = binding.input.text.toString()
                Result.SaveRecordingRequested(
                    filename = filename,
                    copyToMusicFolder = binding.copyToMusicFolder.isChecked
                )
            },
        )
            .onEach { result ->
                listener.saveRecordingResult(result)
                dismiss()
            }

        merge(states, results).launchIn(viewLifecycleOwner.lifecycleScope)
    }

    sealed class Result {
        data class SaveRecordingRequested(val filename: String, val copyToMusicFolder: Boolean) : Result()
        object Delete : Result()
    }

    companion object {
        private const val KEY_DEFAULT_FILENAME = "default_filename"

        fun create(defaultFilename: String): SaveRecordingDialogFragment {
            return SaveRecordingDialogFragment().apply {
                arguments = bundleOf(KEY_DEFAULT_FILENAME to defaultFilename)
            }
        }
    }
}
