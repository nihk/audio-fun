package com.audio.recordings.data

import android.content.Context
import android.os.FileObserver
import com.audio.core.di.AppCoroutineScope
import com.audio.core.di.IoContext
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

interface RecordingsRepository {
    fun recordings(): Flow<List<Recording>>
    fun delete(recording: Recording)
}

class FileSystemRecordingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoContext private val ioContext: CoroutineContext,
    @AppCoroutineScope private val appScope: CoroutineScope
) : RecordingsRepository {
    override fun recordings(): Flow<List<Recording>> {
        return fileRecordings()
            .onStart { emit(readRecordings()) }
            .flowOn(ioContext)
    }

    private fun fileRecordings(): Flow<List<Recording>> = callbackFlow {
        val observer = object : FileObserver(context.filesDir) {
            override fun onEvent(event: Int, path: String?) {
                val wroteFile = event and (DELETE or CLOSE_WRITE) != 0
                // Simply reading a file associated with this observer will call this onEvent back
                // (recursively), so no-op unless it's a write.
                if (!wroteFile) return
                trySend(readRecordings())
            }
        }

        observer.startWatching()

        awaitClose { observer.stopWatching() }
    }

    override fun delete(recording: Recording) {
        appScope.launch {
            val file = File(context.filesDir, recording.name)
            if (!file.exists()) return@launch
            file.delete()
        }
    }

    private fun readRecordings(): List<Recording> {
        return context
            .filesDir
            .listFiles()
            ?.map { file ->
                Recording(name = file.name)
            }
            .orEmpty()
    }
}
