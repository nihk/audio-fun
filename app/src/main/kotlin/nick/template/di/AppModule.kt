package nick.template.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
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
import nick.template.initializers.AppInitializer
import nick.template.initializers.Initializer
import nick.template.initializers.MainInitializer

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
    @IntoSet
    fun mainInitializer(mainInitializer: MainInitializer): Initializer

    @Binds
    fun appInitializers(appInitializer: AppInitializer): Initializer

    @Binds
    fun audioRepository(audioRepository: AndroidAudioRepository): AudioRepository

    @Binds
    fun audioPermissionsRepository(repository: AndroidAudioPermissionsRepository): AudioPermissionsRepository

    @Binds
    fun timestamp(timestamp: SystemTimestamp): Timestamp
}
