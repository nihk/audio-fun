package com.audio.recorder.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import javax.inject.Inject

internal class ConfirmStopRecordingDialogFragment @Inject constructor(
    private val stopRecording: StopRecording
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Stop recording?")
            .setMessage("Recording is still in progress. Do you want to stop that?")
            .setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                stopRecording.stopRecording()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }
}
