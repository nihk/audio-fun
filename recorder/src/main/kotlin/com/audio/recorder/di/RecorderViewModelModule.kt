package com.audio.recorder.di

import com.audio.recorder.data.AndroidRecorderPermissionsRepository
import com.audio.recorder.data.AndroidRecorderRepository
import com.audio.recorder.data.RecorderPermissionsRepository
import com.audio.recorder.data.RecorderRepository
import com.audio.recorder.data.SystemTimestamp
import com.audio.recorder.data.Timestamp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal interface RecorderViewModelModule {
    @Binds
    fun recorderRepository(repository: AndroidRecorderRepository): RecorderRepository

    @Binds
    fun recorderPermissionsRepository(repository: AndroidRecorderPermissionsRepository): RecorderPermissionsRepository

    @Binds
    fun timestamp(timestamp: SystemTimestamp): Timestamp
}
