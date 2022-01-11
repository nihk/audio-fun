package nick.template.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import nick.template.ui.AppFragmentFactory
import nick.template.ui.dialogs.ExternalEvents

@EntryPoint
@InstallIn(FragmentComponent::class)
interface RecorderEntryPoint {
    val fragmentFactory: AppFragmentFactory
    val externalEvents: ExternalEvents
}
