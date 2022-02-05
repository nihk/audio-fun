package com.audio.recorder.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

internal class TellUserToEnablePermissionViaSettingsDialogFragment(
    private val openAppSettings: OpenAppSettings
): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Enable permission in app settings")
            .setMessage("You'll have to enable the record audio permission in app settings if you want to use the app.")
            .setPositiveButton("Open settings") { dialogInterface, _ ->
                dialogInterface.dismiss()
                openAppSettings.openAppSettings()
            }
            .show()
    }
}
