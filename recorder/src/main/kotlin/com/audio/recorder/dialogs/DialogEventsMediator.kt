package com.audio.recorder.dialogs

import com.audio.recorder.data.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class DialogEventsMediator :
    ExternalEvents,
    StopRecording,
    PermissionRationale,
    SaveRecording,
    OpenAppSettings {

    private val events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    override fun events(): Flow<Event> = events

    override fun stopRecording() {
        events.tryEmit(Event.RecordEvent.Stop)
    }

    override fun onPermissionRationaleShown() {
        events.tryEmit(Event.RequestPermissionEvent.General)
    }

    override fun result(result: SaveRecording.Result) {
        val effect = when (result) {
            SaveRecording.Result.Delete -> Event.DeleteSaveRecordingEvent
            is SaveRecording.Result.SaveRecordingRequested -> Event.SaveRecordingEvent(
                filename = result.filename,
                copyToMusicFolder = result.copyToMusicFolder
            )
        }
        events.tryEmit(effect)
    }

    override fun openAppSettings() {
        events.tryEmit(Event.OpenAppSettingsEvent)
    }
}

internal interface ExternalEvents {
    fun events(): Flow<Event>
}

internal interface StopRecording {
    fun stopRecording()
}

internal interface PermissionRationale {
    fun onPermissionRationaleShown()
}

internal interface SaveRecording {
    fun result(result: Result)

    sealed class Result {
        data class SaveRecordingRequested(val filename: String, val copyToMusicFolder: Boolean) : Result()
        object Delete : Result()
    }
}

internal interface OpenAppSettings {
    fun openAppSettings()
}
