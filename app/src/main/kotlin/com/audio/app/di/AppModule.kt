package com.audio.app.di

import com.audio.core.di.AppCoroutineScope
import com.audio.core.di.IoContext
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import com.audio.recorder.data.AndroidAudioPermissionsRepository
import com.audio.recorder.data.AndroidAudioRepository
import com.audio.recorder.data.AudioPermissionsRepository
import com.audio.recorder.data.AudioRepository
import com.audio.recorder.data.SystemTimestamp
import com.audio.recorder.data.Timestamp

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    companion object {
        @Provides
        @IoContext
        fun ioContext(): CoroutineContext = Dispatchers.IO

        @Provides
        @AppCoroutineScope
        fun appScope(): CoroutineScope = GlobalScope
    }

    @Binds
    fun audioRepository(audioRepository: AndroidAudioRepository): AudioRepository

    @Binds
    fun audioPermissionsRepository(repository: AndroidAudioPermissionsRepository): AudioPermissionsRepository

    @Binds
    fun timestamp(timestamp: SystemTimestamp): Timestamp
}
