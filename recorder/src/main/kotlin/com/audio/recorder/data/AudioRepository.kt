package com.audio.recorder.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.contentValuesOf
import com.audio.core.di.AppCoroutineScope
import com.audio.core.di.IoContext
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface AudioRepository {
    fun emissions(): Flow<Emission>
    // todo: pass in some recording config?
    suspend fun start()
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    fun deleteFromCache(cachedFilename: CachedFilename)
    fun save(
        cachedFilename: CachedFilename,
        destinationFilename: String,
        copyToMusicFolder: Boolean,
        cleanupCache: Boolean
    )

    sealed class Emission {
        data class Error(val throwable: Throwable) : Emission()
        data class StartedRecording(val cachedFilename: CachedFilename) : Emission()
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
    @AppCoroutineScope private val appScope: CoroutineScope
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
            Event.Resume -> flowOf(AudioRepository.Emission.ResumedRecording)
            Event.FinishedRecording -> flowOf(AudioRepository.Emission.FinishedRecording)
        }
    }

    override suspend fun start() {
        check(mediaRecorder == null)
        Log.d("asdf", "started recording")
        val extension = "aac"
        val filename = "recording_${timestamp.current()}"
        val absolute = "${context.cacheDir.absolutePath}/$filename.$extension"

        val mediaRecorder = createMediaRecorder().apply {
            // Order matters here. See source code docs.
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setAudioSamplingRate(44100)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(absolute)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
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

    override fun deleteFromCache(cachedFilename: CachedFilename) {
        appScope.launch {
            deleteFromCacheInternal(cachedFilename)
        }
    }

    private suspend fun deleteFromCacheInternal(cachedFilename: CachedFilename) = withContext(ioContext) {
        Log.d("asdf", "deleting file: ${cachedFilename.absolute}")
        File(cachedFilename.absolute).delete()
    }

    override fun save(
        cachedFilename: CachedFilename,
        destinationFilename: String,
        copyToMusicFolder: Boolean,
        cleanupCache: Boolean
    ) {
        appScope.launch(ioContext) {
            val destination = File(context.filesDir, "$destinationFilename.${cachedFilename.extension}")
            val source = File(cachedFilename.absolute)
            source.copyTo(destination)

            if (copyToMusicFolder) {
                writeToMediaStore(cachedFilename, source)
            }

            deleteFromCacheInternal(cachedFilename)
        }
    }

    private fun writeToMediaStore(cachedFilename: CachedFilename, source: File) {
        val musicFolder: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val details = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, cachedFilename.simple)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/${cachedFilename.extension}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }

        val resolver: ContentResolver = context.contentResolver
        val recording: Uri = requireNotNull(resolver.insert(musicFolder, details))
        source.inputStream().use { inputStream ->
            resolver.openOutputStream(recording).use { outputStream ->
                inputStream.copyTo(requireNotNull(outputStream))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(
                recording,
                contentValuesOf(MediaStore.Audio.Media.IS_PENDING to 0),
                null,
                null
            )
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
        object Resume : Event()
        object FinishedRecording : Event()
    }

    companion object {
        private const val AMPLITUDE_UPPER_BOUND = 32767 // 2^(16-1) - 1
    }
}
