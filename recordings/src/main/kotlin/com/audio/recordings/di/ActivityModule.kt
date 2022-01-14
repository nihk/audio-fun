package com.audio.recordings.di

import androidx.fragment.app.Fragment
import com.audio.core.di.FragmentKey
import com.audio.recordings.ui.RecordingsFragment
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
    @FragmentKey(RecordingsFragment::class)
    fun recordingsFragment(fragment: RecordingsFragment): Fragment
}
