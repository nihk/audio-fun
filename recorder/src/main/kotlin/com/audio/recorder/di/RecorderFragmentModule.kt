package com.audio.recorder.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.audio.core.di.FragmentKey
import com.audio.core.ui.AppFragmentFactory
import com.audio.recorder.dialogs.ConfirmStopRecordingDialogFragment
import com.audio.recorder.dialogs.DialogEventsMediator
import com.audio.recorder.dialogs.ExternalEvents
import com.audio.recorder.dialogs.OpenAppSettings
import com.audio.recorder.dialogs.PermissionRationale
import com.audio.recorder.dialogs.PermissionRationaleDialogFragment
import com.audio.recorder.dialogs.SaveRecording
import com.audio.recorder.dialogs.SaveRecordingDialogFragment
import com.audio.recorder.dialogs.StopRecording
import com.audio.recorder.dialogs.TellUserToEnablePermissionViaSettingsDialogFragment
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(FragmentComponent::class)
internal interface RecorderFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(ConfirmStopRecordingDialogFragment::class)
    fun confirmStopRecordingDialogFragment(fragment: ConfirmStopRecordingDialogFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PermissionRationaleDialogFragment::class)
    fun permissionRationaleDialogFragment(fragment: PermissionRationaleDialogFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SaveRecordingDialogFragment::class)
    fun saveRecordingDialogFragment(fragment: SaveRecordingDialogFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(TellUserToEnablePermissionViaSettingsDialogFragment::class)
    fun tellUserToEnablePermissionViaSettingsDialogFragment(fragment: TellUserToEnablePermissionViaSettingsDialogFragment): Fragment

    @Binds
    fun fragmentFactory(fragmentFactory: AppFragmentFactory): FragmentFactory

    @Binds
    fun stopRecording(impl: DialogEventsMediator): StopRecording

    @Binds
    fun externalEffects(impl: DialogEventsMediator): ExternalEvents

    @Binds
    fun permissionRationale(impl: DialogEventsMediator): PermissionRationale

    @Binds
    fun saveRecording(impl: DialogEventsMediator): SaveRecording

    @Binds
    fun openAppSettings(impl: DialogEventsMediator): OpenAppSettings
}
