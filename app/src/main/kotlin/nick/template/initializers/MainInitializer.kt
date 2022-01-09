package nick.template.initializers

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Inject
import nick.template.DefaultActivityLifecycleCallbacks
import nick.template.ui.MainActivity
import nick.template.ui.extensions.entryPoint

class MainInitializer @Inject constructor(private val application: Application) : Initializer {
    override fun initialize() {
        val callbacks = object : DefaultActivityLifecycleCallbacks() {
            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity !is MainActivity) return
                activity.supportFragmentManager.fragmentFactory = activity.entryPoint<Injection>().fragmentFactory
            }
        }
        application.registerActivityLifecycleCallbacks(callbacks)
    }

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface Injection {
        val fragmentFactory: FragmentFactory
    }
}
