package com.audio.recorder.di

import com.audio.recorder.data.AndroidAudioPermissionsRepository
import com.audio.recorder.data.AndroidAudioRepository
import com.audio.recorder.data.AudioPermissionsRepository
import com.audio.recorder.data.AudioRepository
import com.audio.recorder.data.SystemTimestamp
import com.audio.recorder.data.Timestamp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface RecorderViewModelModule {
    @Binds
    fun audioRepository(audioRepository: AndroidAudioRepository): AudioRepository

    @Binds
    fun audioPermissionsRepository(repository: AndroidAudioPermissionsRepository): AudioPermissionsRepository

    @Binds
    fun timestamp(timestamp: SystemTimestamp): Timestamp
}
