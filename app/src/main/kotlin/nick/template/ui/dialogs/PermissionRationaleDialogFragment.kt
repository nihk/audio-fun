package nick.template.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import javax.inject.Inject

class PermissionRationaleDialogFragment @Inject constructor(
    private val permissionRationale: PermissionRationale
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Yeah.. I'm gonna need you to grant that permission")
            .setMessage("Kinda makes sense that if you want to record audio, you should grant this app permission to record audio, right?")
            .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                dialogInterface.dismiss()
                permissionRationale.onPermissionRationaleShown()
            }
            .show()
    }
}
