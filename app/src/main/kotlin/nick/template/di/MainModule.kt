package nick.template.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import nick.template.ui.AppFragmentFactory
import nick.template.ui.MainFragment

@Module
@InstallIn(ActivityComponent::class)
interface MainModule {
    companion object {
    }

    @Binds
    @IntoMap
    @FragmentKey(MainFragment::class)
    fun mainFragment(mainFragment: MainFragment): Fragment

    @Binds
    fun fragmentFactory(appFragmentFactory: AppFragmentFactory): FragmentFactory
}
