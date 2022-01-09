package nick.template.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class PermissionRationaleDialogFragment : DialogFragment() {
    private lateinit var listener: Listener

    interface Listener {
        fun onRationaleExplained()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isCancelable = false
        listener = parentFragment as Listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Yeah.. I'm gonna need you to grant that permission")
            .setMessage("Kinda makes sense that if you want to record audio, you should grant this app permission to record audio, right?")
            .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                dialogInterface.dismiss()
                listener.onRationaleExplained()
            }
            .show()
    }
}
