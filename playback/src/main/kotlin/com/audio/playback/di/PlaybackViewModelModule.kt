package com.audio.playback.di

import com.audio.playback.data.MediaPlayerPlaybackRepository
import com.audio.playback.data.PlaybackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface PlaybackViewModelModule {
    @Binds
    fun playbackRepository(repository: MediaPlayerPlaybackRepository): PlaybackRepository
}
