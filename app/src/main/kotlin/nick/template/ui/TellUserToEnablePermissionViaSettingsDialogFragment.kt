package nick.template.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class TellUserToEnablePermissionViaSettingsDialogFragment : DialogFragment() {
    private lateinit var listener: Listener

    interface Listener {
        fun openAppSettings()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as Listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Enable permission in app settings")
            .setMessage("You'll have to enable the record audio permission in app settings if you want to use the app.")
            .setPositiveButton("Open settings") { dialogInterface, _ ->
                dialogInterface.dismiss()
                listener.openAppSettings()
            }
            .show()
    }
}
