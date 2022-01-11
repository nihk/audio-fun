package nick.template.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntoMap
import nick.template.ui.AppFragmentFactory
import nick.template.ui.dialogs.ConfirmStopRecordingDialogFragment
import nick.template.ui.dialogs.DialogEventsMediator
import nick.template.ui.dialogs.ExternalEvents
import nick.template.ui.dialogs.OpenAppSettings
import nick.template.ui.dialogs.PermissionRationale
import nick.template.ui.dialogs.PermissionRationaleDialogFragment
import nick.template.ui.dialogs.SaveRecording
import nick.template.ui.dialogs.SaveRecordingDialogFragment
import nick.template.ui.dialogs.StopRecording
import nick.template.ui.dialogs.TellUserToEnablePermissionViaSettingsDialogFragment

@Module
@InstallIn(FragmentComponent::class)
interface RecorderFragmentModule {
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
