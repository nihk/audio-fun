package com.audio.playback.di

import androidx.fragment.app.Fragment
import com.audio.core.di.FragmentKey
import com.audio.playback.ui.PlaybackFragment
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
    @FragmentKey(PlaybackFragment::class)
    fun playbackFragment(fragment: PlaybackFragment): Fragment
}
