package com.audio.files

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.FileObserver
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.contentValuesOf
import com.audio.core.di.AppCoroutineScope
import com.audio.core.di.IoContext
import com.audio.core.ui.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface AudioFilesystem {
    // This doesn't create any file, it just returns a handle to a temporary path
    fun tempFilename(name: String): Filename
    fun files(): Flow<List<File>>
    suspend fun file(name: String): File
    fun delete(filename: String)
    fun save(filename: Filename, newName: String, copyToMusicFolder: Boolean)
}

class AndroidAudioFilesystem @Inject constructor(
    private val context: ApplicationContext,
    @IoContext private val ioContext: CoroutineContext,
    @AppCoroutineScope private val appScope: CoroutineScope
) : AudioFilesystem {
    override fun tempFilename(name: String): Filename {
        return Filename(
            simple = name.popExtension,
            absolute = "${context.cacheDir.absolutePath}/$name",
            extension = name.extension
        )
    }

    override fun files(): Flow<List<File>> {
        return filesInternal()
            .onStart { emit(snapshot()) }
            .flowOn(ioContext)
    }

    private fun filesInternal(): Flow<List<File>> = callbackFlow {
        Log.d("asdf", "started observing files")
            val observer = object : FileObserver(context.filesDir) {
            override fun onEvent(event: Int, path: String?) {
                val wroteFile = event and (DELETE or CLOSE_WRITE) != 0
                // Simply reading a file associated with this observer will call this onEvent back
                // (recursively), so no-op unless it's a write.
                if (!wroteFile) return
                trySend(snapshot())
            }
        }

        observer.startWatching()

        awaitClose {
            Log.d("asdf", "stopped observing files")
            observer.stopWatching()
        }
    }

    private fun snapshot(): List<File> {
        return context.filesDir.listFiles().orEmpty().toList()
    }

    override suspend fun file(name: String): File {
        return File(context.filesDir, name)
    }

    override fun delete(filename: String) {
        appScope.launch(ioContext) {
            val file = File(context.filesDir, filename)
            if (!file.exists()) return@launch
            file.delete()
        }
    }

    override fun save(
        filename: Filename,
        newName: String,
        copyToMusicFolder: Boolean
    ) {
        appScope.launch(ioContext) {
            val destination = File(context.filesDir, "$newName.${filename.extension}")
            if (destination.exists()) {
                destination.delete()
            }

            val source = File(filename.absolute)
            source.copyTo(destination)

            if (copyToMusicFolder) {
                writeToMediaStore(filename, source)
            }

            deleteFromCacheInternal(filename)
        }
    }

    private fun writeToMediaStore(filename: Filename, source: File) {
        val musicFolder: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val details = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, filename.simple)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/${filename.extension}")
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

    private suspend fun deleteFromCacheInternal(filename: Filename) = withContext(ioContext) {
        Log.d("asdf", "deleting file: ${filename.absolute}")
        File(filename.absolute).delete()
    }

    private val String.extension: String get() = substringAfterLast(".", "")

    private val String.popExtension: String get() = substringBeforeLast(".", "")
}
