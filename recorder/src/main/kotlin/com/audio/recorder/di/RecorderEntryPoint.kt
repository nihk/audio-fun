package com.audio.recorder.di

import com.audio.core.ui.AppFragmentFactory
import com.audio.recorder.dialogs.ExternalEvents
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@EntryPoint
@InstallIn(FragmentComponent::class)
interface RecorderEntryPoint {
    val fragmentFactory: AppFragmentFactory
    val externalEvents: ExternalEvents
}
