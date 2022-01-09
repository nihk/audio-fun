package nick.template.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow
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
    suspend fun deleteFromCache(filename: String)
    suspend fun save(cachedFilename: String, destinationFilename: String)

    interface RecordingConfig

    sealed class Emission {
        data class Error(val throwable: Throwable) : Emission()
        data class Recording(val cachedFilename: String) : Emission()
        data class Amplitude(val value: Int) : Emission()
    }
}

class AndroidAudioRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoContext private val ioContext: CoroutineContext,
    private val timestamp: Timestamp
) : AudioRepository {
    override fun record() = callbackFlow {
        Log.d("asdf", "started recording")
        val filename = "recording_${timestamp.current()}"
        val absoluteFilename = "${context.cacheDir.absolutePath}/$filename.3gp"

        val recorder = createMediaRecorder().apply {
            // Order matters here. See source code docs.
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(absoluteFilename)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            val emission = try {
                prepare()
                start()
                AudioRepository.Emission.Recording(absoluteFilename)
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
        return (this / (2.0f.pow(16) - 1) * 100).roundToInt()
    }

    override suspend fun deleteFromCache(filename: String): Unit = withContext(ioContext) {
        Log.d("asdf", "deleting file: $filename")
        File(filename).delete()
    }

    override suspend fun save(cachedFilename: String, destinationFilename: String): Unit = withContext(ioContext) {
        Log.d("asdf", "moving file from cache to mediastore")
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
}
