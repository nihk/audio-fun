package nick.template.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import nick.template.R
import nick.template.databinding.SaveRecordingDialogFragmentBinding
import nick.template.ui.extensions.clicks
import nick.template.ui.extensions.focusAndShowKeyboard
import nick.template.ui.extensions.textChanges

class SaveRecordingDialogFragment @Inject constructor(
    private val saveRecording: SaveRecording
) : DialogFragment(R.layout.save_recording_dialog_fragment) {

    override fun onAttach(context: Context) {
        super.onAttach(context)
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
            binding.delete.clicks().map { SaveRecording.Result.Delete },
            binding.save.clicks().map {
                val filename = binding.input.text.toString()
                SaveRecording.Result.SaveRecordingRequested(
                    filename = filename,
                    copyToMusicFolder = binding.copyToMusicFolder.isChecked
                )
            },
        )
            .onEach { result ->
                saveRecording.result(result)
                dismiss()
            }

        merge(states, results).launchIn(viewLifecycleOwner.lifecycleScope)

        // Hack to make dialog fill the window width
        requireDialog().window?.let { window ->
            window.attributes = window.attributes.also { it.width = ViewGroup.LayoutParams.MATCH_PARENT }
        }
    }

    companion object {
        private const val KEY_DEFAULT_FILENAME = "default_filename"

        fun bundle(defaultFilename: String): Bundle {
            return bundleOf(KEY_DEFAULT_FILENAME to defaultFilename)
        }
    }
}
