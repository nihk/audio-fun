package com.audio.recorder.di

import androidx.fragment.app.Fragment
import com.audio.core.di.CoreGraph
import com.audio.core.ui.AppFragmentFactory
import com.audio.files.di.FilesGraph
import com.audio.recorder.data.AndroidRecorderPermissionsRepository
import com.audio.recorder.data.AndroidRecorderRepository
import com.audio.recorder.data.SystemTimestamp
import com.audio.recorder.dialogs.ConfirmStopRecordingDialogFragment
import com.audio.recorder.dialogs.DialogEventsMediator
import com.audio.recorder.dialogs.PermissionRationaleDialogFragment
import com.audio.recorder.dialogs.SaveRecordingDialogFragment
import com.audio.recorder.dialogs.TellUserToEnablePermissionViaSettingsDialogFragment
import com.audio.recorder.ui.RecorderFragment
import com.audio.recorder.ui.RecorderViewModel

class RecorderGraph(
    private val coreGraph: CoreGraph
) {
    val recorderFragment: Pair<Class<out Fragment>, () -> Fragment>
        get() {
            return RecorderFragment::class.java to {
                val dialogEventsMediator = DialogEventsMediator()

                RecorderFragment(
                    viewModelFactory = { owner ->
                        RecorderViewModel.Factory(
                            recorderRepository = AndroidRecorderRepository(
                                context = coreGraph.appContext,
                                ioContext = coreGraph.ioContext,
                                timestamp = SystemTimestamp(),
                                filesystem = FilesGraph(coreGraph).audioFilesystem
                            ),
                            permissionsRepository = AndroidRecorderPermissionsRepository(
                                context = coreGraph.appContext
                            )
                        ).create(owner)
                    },
                    fragmentFactory = AppFragmentFactory(
                        fragments = mapOf(
                            ConfirmStopRecordingDialogFragment::class.java to {
                                ConfirmStopRecordingDialogFragment(dialogEventsMediator)
                            },
                            PermissionRationaleDialogFragment::class.java to {
                                PermissionRationaleDialogFragment(dialogEventsMediator)
                            },
                            SaveRecordingDialogFragment::class.java to {
                                SaveRecordingDialogFragment(dialogEventsMediator)
                            },
                            TellUserToEnablePermissionViaSettingsDialogFragment::class.java to {
                                TellUserToEnablePermissionViaSettingsDialogFragment(dialogEventsMediator)
                            }
                        )
                    ),
                    externalEvents = dialogEventsMediator
                )
            }
        }
}
