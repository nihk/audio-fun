package nick.template

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import nick.template.initializers.Initializer

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var initializer: Initializer

    override fun onCreate() {
        super.onCreate()
        initializer.initialize()
    }
}
