package com.audio.recorder.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.audio.core.di.IoContext
import com.audio.files.AudioFilesystem
import com.audio.files.Filename
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
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

interface AudioRepository {
    fun emissions(): Flow<Emission>
    // todo: pass in some recording config?
    suspend fun start()
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    fun cleanup(filename: Filename)
    fun save(
        cachedFilename: Filename,
        destinationFilename: String,
        copyToMusicFolder: Boolean
    )

    sealed class Emission {
        data class Error(val throwable: Throwable) : Emission()
        data class StartedRecording(val cachedFilename: Filename) : Emission()
        data class Amplitude(val value: Int) : Emission()
        object PausedRecording : Emission()
        object ResumedRecording : Emission()
        object FinishedRecording : Emission()
    }
}

class AndroidAudioRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoContext private val ioContext: CoroutineContext,
    private val timestamp: Timestamp,
    private val filesystem: AudioFilesystem
) : AudioRepository {
    private val events = MutableSharedFlow<Event>()
    private var mediaRecorder: MediaRecorder? = null

    override fun emissions() = events.flatMapLatest { event ->
        when (event) {
            is Event.StartedRecording -> flowOf(AudioRepository.Emission.StartedRecording(event.filename))
            Event.PollAmplitude -> flow {
                Log.d("asdf", "polling amplitude")
                val recorder = requireNotNull(mediaRecorder)
                while (currentCoroutineContext().isActive) {
                    emit(AudioRepository.Emission.Amplitude(recorder.maxAmplitude.scaled()))
                    delay(500L)
                }
            }
            is Event.Error -> flowOf(AudioRepository.Emission.Error(event.throwable))
            Event.Pause -> flowOf(AudioRepository.Emission.PausedRecording)
            Event.Resume -> flowOf(AudioRepository.Emission.ResumedRecording)
            Event.FinishedRecording -> flowOf(AudioRepository.Emission.FinishedRecording)
        }
    }

    override suspend fun start() {
        check(mediaRecorder == null)
        Log.d("asdf", "started recording")
        val filename = filesystem.filename(
            name = "recording_${timestamp.current()}.aac"
        )

        val mediaRecorder = createMediaRecorder().apply {
            // Order matters here. See source code docs.
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setAudioSamplingRate(44100)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
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

    override fun save(cachedFilename: Filename, destinationFilename: String, copyToMusicFolder: Boolean) {
        filesystem.save(cachedFilename, destinationFilename, copyToMusicFolder)
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
