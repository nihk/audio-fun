package com.audio.recordings.data

import com.audio.files.AudioFilesystem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface RecordingsRepository {
    fun recordings(): Flow<List<Recording>>
    fun delete(recording: Recording)
}

class FilesystemRecordingsRepository @Inject constructor(
    private val filesystem: AudioFilesystem
) : RecordingsRepository {
    override fun recordings(): Flow<List<Recording>> {
        return filesystem.files().map { files ->
            files.map { file ->
                Recording(
                    name = file.name,
                    absolute = file.absolutePath
                )
            }
        }
    }

    override fun delete(recording: Recording) {
        filesystem.delete(recording.name)
    }
}
