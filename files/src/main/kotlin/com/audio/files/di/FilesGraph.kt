package com.audio.files.di

import com.audio.core.di.CoreGraph
import com.audio.files.AndroidAudioFilesystem
import com.audio.files.AudioFilesystem

class FilesGraph(private val coreGraph: CoreGraph) {
    val audioFilesystem: AudioFilesystem get() = AndroidAudioFilesystem(
        context = coreGraph.appContext,
        ioContext = coreGraph.ioContext,
        appScope = coreGraph.appScope
    )
}
