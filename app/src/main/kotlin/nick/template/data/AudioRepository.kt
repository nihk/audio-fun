package nick.template.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nick.template.di.AppScope
import nick.template.di.IoContext

interface AudioRepository {
    fun emissions(): Flow<Emission>
    // todo: pass in some recording config
    suspend fun start()
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    fun deleteFromCache(cachedFilename: CachedFilename)
    fun save(cachedFilename: CachedFilename, destinationFilename: String, copyToMusicFolder: Boolean)

    interface RecordingConfig

    sealed class Emission {
        data class Error(val throwable: Throwable) : Emission()
        data class StartedRecording(val cachedFilename: CachedFilename) : Emission()
        data class Amplitude(val value: Int) : Emission()
        object PausedRecording : Emission()
        object FinishedRecording : Emission()
    }
}

class AndroidAudioRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoContext private val ioContext: CoroutineContext,
    private val timestamp: Timestamp,
    @AppScope private val appScope: CoroutineScope
) : AudioRepository {
    private val events = MutableSharedFlow<Event>()
    private var mediaRecorder: MediaRecorder? = null

    override fun emissions() = events.flatMapLatest { event ->
        when (event) {
            is Event.StartedRecording -> flowOf(AudioRepository.Emission.StartedRecording(event.cachedFilename))
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
            Event.FinishedRecording -> flowOf(AudioRepository.Emission.FinishedRecording)
        }
    }

    override suspend fun start() {
        check(mediaRecorder == null)
        Log.d("asdf", "started recording")
        val extension = "3gp"
        val filename = "recording_${timestamp.current()}"
        val absolute = "${context.cacheDir.absolutePath}/$filename.$extension"

        val mediaRecorder = createMediaRecorder().apply {
            // Order matters here. See source code docs.
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(absolute)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        }.also { this.mediaRecorder = it }

        try {
            withContext(ioContext) {
                mediaRecorder.prepare()
            }
            mediaRecorder.start()
            val cachedFilename = CachedFilename(
                simple = filename,
                absolute = absolute,
                extension = extension
            )
            events.emit(Event.StartedRecording(cachedFilename))
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

    override fun deleteFromCache(cachedFilename: CachedFilename) {
        appScope.launch(ioContext) {
            Log.d("asdf", "deleting file: ${cachedFilename.absolute}")
            File(cachedFilename.absolute).delete()
        }
    }

    override fun save(cachedFilename: CachedFilename, destinationFilename: String, copyToMusicFolder: Boolean) {
        appScope.launch(ioContext) {
            // todo: don't forget to slap on a file extension to destinationFilename. maybe use Uri.parse for this
        }
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
        data class StartedRecording(val cachedFilename: CachedFilename) : Event()
        object PollAmplitude : Event()
        data class Error(val throwable: Throwable) : Event()
        object Pause : Event()
        object FinishedRecording : Event()
    }

    companion object {
        private const val AMPLITUDE_UPPER_BOUND = 32767 // 2^(16-1) - 1
    }
}
