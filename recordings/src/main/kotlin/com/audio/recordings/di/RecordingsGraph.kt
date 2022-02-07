package com.audio.recordings.di

import androidx.fragment.app.Fragment
import com.audio.core.di.CoreGraph
import com.audio.files.di.FilesGraph
import com.audio.recordings.data.FilesystemRecordingsRepository
import com.audio.recordings.ui.RecordingsFragment
import com.audio.recordings.ui.RecordingsNavigator
import com.audio.recordings.ui.RecordingsViewModel

class RecordingsGraph(
    private val navigator: RecordingsNavigator,
    private val coreGraph: CoreGraph
) {
    val recordingsFragment: Pair<Class<out Fragment>, () -> Fragment> get() {
        return RecordingsFragment::class.java to {
            RecordingsFragment(
                navigator = navigator,
                viewModelFactory = { owner ->
                    RecordingsViewModel.Factory(
                        repository = FilesystemRecordingsRepository(
                            filesystem = FilesGraph(coreGraph).audioFilesystem
                        )
                    ).create(owner)
                }
            )
        }
    }
}
