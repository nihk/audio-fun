package com.audio.app.di

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import com.audio.core.di.FragmentKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import com.audio.R
import com.audio.core.ui.AppFragmentFactory
import com.audio.recorder.ui.RecorderFragment
import com.audio.recordings.ui.RecordingsFragment
import com.audio.recordings.ui.RecordingsNavigator
import com.audio.app.ui.navigation.FragmentRecordingsNavigator

@Module
@InstallIn(ActivityComponent::class)
interface MainModule {
    companion object {
        @Provides
        @FragmentContainerId
        fun fragmentContainerId() = R.id.fragment_container

        @Provides
        fun fragmentManager(activity: Activity): FragmentManager {
            return (activity as FragmentActivity).supportFragmentManager
        }
    }

    @Binds
    @IntoMap
    @FragmentKey(RecorderFragment::class)
    fun recorderFragment(fragment: RecorderFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RecordingsFragment::class)
    fun recordingsFragment(fragment: RecordingsFragment): Fragment

    @Binds
    fun fragmentFactory(fragmentFactory: AppFragmentFactory): FragmentFactory

    @Binds
    fun recordingsNavigator(navigator: FragmentRecordingsNavigator): RecordingsNavigator
}
