package com.audio.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

@Module
@InstallIn(SingletonComponent::class)
interface CoreModule {
    companion object {
        @Provides
        @IoContext
        fun ioContext(): CoroutineContext = Dispatchers.IO

        @Provides
        @AppCoroutineScope
        fun appScope(): CoroutineScope = GlobalScope
    }
}
