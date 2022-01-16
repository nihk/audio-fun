package com.audio.recordings.ui

interface RecordingsNavigator {
    fun toRecorder()
    fun toPlayback(recordingName: String)
}
