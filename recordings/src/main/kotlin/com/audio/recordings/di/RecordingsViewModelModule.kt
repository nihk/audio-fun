package com.audio.recordings.di

import com.audio.recordings.data.FilesystemRecordingsRepository
import com.audio.recordings.data.RecordingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface RecordingsViewModelModule {
    @Binds
    fun recordingsRepository(repository: FilesystemRecordingsRepository): RecordingsRepository
}
