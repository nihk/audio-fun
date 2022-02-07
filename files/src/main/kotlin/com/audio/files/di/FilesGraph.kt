package com.audio.files.di

import com.audio.core.di.CoreGraph
import com.audio.files.AndroidAudioFilesystem
import com.audio.files.AudioFilesystem

class FilesGraph(coreGraph: CoreGraph) {
    val audioFilesystem: AudioFilesystem = AndroidAudioFilesystem(
        context = coreGraph.appContext,
        ioContext = coreGraph.ioContext,
        appScope = coreGraph.appScope
    )
}
