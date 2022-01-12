package nick.template.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import nick.template.data.AndroidAudioPermissionsRepository
import nick.template.data.AndroidAudioRepository
import nick.template.data.AudioPermissionsRepository
import nick.template.data.AudioRepository
import nick.template.data.SystemTimestamp
import nick.template.data.Timestamp

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    companion object {
        @Provides
        @IoContext
        fun ioContext(): CoroutineContext = Dispatchers.IO

        @Provides
        @AppScope
        fun appScope(): CoroutineScope = GlobalScope
    }

    @Binds
    fun audioRepository(audioRepository: AndroidAudioRepository): AudioRepository

    @Binds
    fun audioPermissionsRepository(repository: AndroidAudioPermissionsRepository): AudioPermissionsRepository

    @Binds
    fun timestamp(timestamp: SystemTimestamp): Timestamp
}
