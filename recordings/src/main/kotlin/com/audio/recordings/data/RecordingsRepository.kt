package com.audio.recordings.data

import com.audio.files.AudioFilesystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal interface RecordingsRepository {
    fun recordings(): Flow<List<Recording>>
    fun delete(recording: Recording)
}

internal class FilesystemRecordingsRepository(
    private val filesystem: AudioFilesystem
) : RecordingsRepository {
    override fun recordings(): Flow<List<Recording>> {
        return filesystem.files().map { files ->
            files
                .map { file ->
                    Recording(
                        name = file.name,
                        absolute = file.absolutePath,
                        lastModified = file.lastModified()
                    )
                }
                .sortedByDescending(Recording::lastModified)
        }
    }

    override fun delete(recording: Recording) {
        filesystem.delete(recording.name)
    }
}
