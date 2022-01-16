package com.audio.recordings.data

import android.content.Context
import android.os.FileObserver
import com.audio.core.di.IoContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

interface RecordingsRepository {
    fun recordings(): Flow<List<Recording>>
}

class FileSystemRecordingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoContext private val ioContext: CoroutineContext
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
