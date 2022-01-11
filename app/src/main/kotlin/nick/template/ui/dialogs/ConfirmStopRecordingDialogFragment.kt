package nick.template.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmStopRecordingDialogFragment : DialogFragment() {
    private lateinit var listener: Listener

    interface Listener {
        fun choice(stopRecording: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as Listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Stop recording?")
            .setMessage("Recording is still in progress. Do you want to stop that?")
            .setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                listener.choice(stopRecording = true)
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
                listener.choice(stopRecording = false)
            }
            .show()
    }
}
