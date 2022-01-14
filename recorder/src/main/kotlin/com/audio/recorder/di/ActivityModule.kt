package com.audio.recorder.di

import androidx.fragment.app.Fragment
import com.audio.core.di.FragmentKey
import com.audio.recorder.ui.RecorderFragment
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(ActivityComponent::class)
interface ActivityModule {
    @Binds
    @IntoMap
    @FragmentKey(RecorderFragment::class)
    fun recorderFragment(fragment: RecorderFragment): Fragment
}
