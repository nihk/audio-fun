package com.audio.recorder.data

import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.audio.core.ui.ApplicationContext
import com.audio.files.AudioFilesystem
import com.audio.files.Filename
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

internal interface RecorderRepository {
    fun emissions(): Flow<Emission>
    // todo: pass in some recording config?
    suspend fun start()
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    fun cleanup(filename: Filename)
    fun save(
        tempFilename: Filename,
        newName: String,
        copyToMusicFolder: Boolean
    )

    sealed class Emission {
        data class Error(val throwable: Throwable) : Emission()
        data class StartedRecording(val tempFilename: Filename) : Emission()
        data class Amplitude(val value: Int) : Emission()
        object PausedRecording : Emission()
        object ResumedRecording : Emission()
        object FinishedRecording : Emission()
    }
}

internal class AndroidRecorderRepository(
    private val context: ApplicationContext,
    private val ioContext: CoroutineContext,
    private val timestamp: Timestamp,
    private val filesystem: AudioFilesystem
) : RecorderRepository {
    private val events = MutableSharedFlow<Event>()
    private var mediaRecorder: MediaRecorder? = null

    override fun emissions() = events.flatMapLatest { event ->
        when (event) {
            is Event.StartedRecording -> flowOf(RecorderRepository.Emission.StartedRecording(event.filename))
            Event.PollAmplitude -> flow {
                Log.d("asdf", "polling amplitude")
                val recorder = requireNotNull(mediaRecorder)
                while (currentCoroutineContext().isActive) {
                    emit(RecorderRepository.Emission.Amplitude(recorder.maxAmplitude.scaled()))
                    delay(200L)
                }
            }
            is Event.Error -> flowOf(RecorderRepository.Emission.Error(event.throwable))
            Event.Pause -> flowOf(RecorderRepository.Emission.PausedRecording)
            Event.Resume -> flowOf(RecorderRepository.Emission.ResumedRecording)
            Event.FinishedRecording -> flowOf(RecorderRepository.Emission.FinishedRecording)
        }
    }

    override suspend fun start() {
        check(mediaRecorder == null)
        Log.d("asdf", "started recording")
        val outputFormat = MediaRecorder.OutputFormat.AAC_ADTS
        val filename = filesystem.tempFilename(
            name = "recording_${timestamp.current()}.${outputFormat.outputFormatExtension()}"
        )

        val mediaRecorder = createMediaRecorder().apply {
            // Order matters here. See source code docs.
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFormat(outputFormat)
            setOutputFile(filename.absolute)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }.also { this.mediaRecorder = it }

        try {
            withContext(ioContext) {
                mediaRecorder.prepare()
            }
            mediaRecorder.start()
            events.emit(Event.StartedRecording(filename))
            events.emit(Event.PollAmplitude)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            events.emit(Event.Error(throwable))
            this.mediaRecorder = null
        }
    }

    private fun Int.outputFormatExtension(): String {
        return when (this) {
            MediaRecorder.OutputFormat.MPEG_4 -> "mp4"
            MediaRecorder.OutputFormat.AAC_ADTS -> "aac"
            else -> error("Unknown output format: $this")
        }
    }

    override suspend fun pause() {
        Log.d("asdf", "paused recording")
        requireNotNull(mediaRecorder).pause()
        events.emit(Event.Pause)
    }

    override suspend fun resume() {
        Log.d("asdf", "resumed recording")
        requireNotNull(mediaRecorder).resume()
        events.emit(Event.Resume)
        events.emit(Event.PollAmplitude)
    }

    override suspend fun stop() {
        Log.d("asdf", "stopped recording")
        requireNotNull(mediaRecorder).apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
        events.emit(Event.FinishedRecording)
    }

    private fun Int.scaled(): Int {
        return (this / AMPLITUDE_UPPER_BOUND.toFloat() * 100).roundToInt()
    }

    override fun cleanup(filename: Filename) {
        filesystem.delete(filename.absolute)
    }

    override fun save(tempFilename: Filename, newName: String, copyToMusicFolder: Boolean) {
        filesystem.save(tempFilename, newName, copyToMusicFolder)
    }

    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    private sealed class Event {
        data class StartedRecording(val filename: Filename) : Event()
        object PollAmplitude : Event()
        data class Error(val throwable: Throwable) : Event()
        object Pause : Event()
        object Resume : Event()
        object FinishedRecording : Event()
    }

    companion object {
        private const val AMPLITUDE_UPPER_BOUND = 32767 // 2^(16-1) - 1
    }
}
