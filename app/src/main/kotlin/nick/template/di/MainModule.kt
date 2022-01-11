package nick.template.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import nick.template.ui.AppFragmentFactory
import nick.template.ui.RecorderFragment

@Module
@InstallIn(ActivityComponent::class)
interface MainModule {
    companion object {
    }

    @Binds
    @IntoMap
    @FragmentKey(RecorderFragment::class)
    fun recorderFragment(fragment: RecorderFragment): Fragment

    @Binds
    fun fragmentFactory(fragmentFactory: AppFragmentFactory): FragmentFactory
}
