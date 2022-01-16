package com.audio.files.di

import com.audio.files.AndroidAudioFilesystem
import com.audio.files.AudioFilesystem
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FileModule {
    @Binds
    fun fileSystem(fileSystem: AndroidAudioFilesystem): AudioFilesystem
}
