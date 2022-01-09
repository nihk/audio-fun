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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import nick.template.di.IoContext

interface AudioRepository {
    // todo: pass in some recording config
    fun record(): Flow<Emission>
    suspend fun deleteFromCache(cachedFilename: CachedFilename)
    suspend fun save(cachedFilename: CachedFilename, destinationFilename: String, copyToMusicFolder: Boolean)

    interface RecordingConfig

    sealed class Emission {
        data class Error(val throwable: Throwable) : Emission()
        data class Recording(val cachedFilename: CachedFilename) : Emission()
        data class Amplitude(val value: Int) : Emission()
    }
}

// todo: inject external coroutine context (App-scoped) for file saving/deleting so it's not tied to VM scope
class AndroidAudioRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoContext private val ioContext: CoroutineContext,
    private val timestamp: Timestamp
) : AudioRepository {
    override fun record() = callbackFlow {
        Log.d("asdf", "started recording")
        val extension = "3gp"
        val filename = "recording_${timestamp.current()}"
        val absolute = "${context.cacheDir.absolutePath}/$filename.$extension"

        val recorder = createMediaRecorder().apply {
            // Order matters here. See source code docs.
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(absolute)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            val emission = try {
                prepare()
                start()
                val cachedFilename = CachedFilename(
                    simple = filename,
                    absolute = absolute,
                    extension = extension
                )
                AudioRepository.Emission.Recording(cachedFilename)
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                AudioRepository.Emission.Error(throwable)
            }
            trySend(emission)
        }

        while (currentCoroutineContext().isActive) {
            trySend(AudioRepository.Emission.Amplitude(recorder.maxAmplitude.scaled()))
            delay(500L)
        }

        awaitClose {
            Log.d("asdf", "stopped recording")
            recorder.apply {
                stop()
                reset()
                release()
            }
        }
    }

    private fun Int.scaled(): Int {
        return (this / AMPLITUDE_UPPER_BOUND.toFloat() * 100).roundToInt()
    }

    override suspend fun deleteFromCache(cachedFilename: CachedFilename): Unit = withContext(ioContext) {
        Log.d("asdf", "deleting file: ${cachedFilename.absolute}")
        File(cachedFilename.absolute).delete()
    }

    override suspend fun save(cachedFilename: CachedFilename, destinationFilename: String, copyToMusicFolder: Boolean): Unit = withContext(ioContext) {
        // todo: don't forget to slap on a file extension to destinationFilename. maybe use Uri.parse for this
    }

    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    companion object {
        private const val AMPLITUDE_UPPER_BOUND = 32767 // 2^(16-1) - 1
    }
}
