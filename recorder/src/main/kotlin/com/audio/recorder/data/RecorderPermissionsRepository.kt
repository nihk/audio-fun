package com.audio.recorder.data

import android.Manifest
import com.audio.core.ui.ApplicationContext

internal interface RecorderPermissionsRepository {
    fun permission(): String
    fun appSettingsParts(): AppSettingsParts
}

internal class AndroidRecorderPermissionsRepository(
    private val context: ApplicationContext
) : RecorderPermissionsRepository {
    override fun permission(): String {
        return Manifest.permission.RECORD_AUDIO
    }

    override fun appSettingsParts(): AppSettingsParts {
        return AppSettingsParts(
            scheme = "package",
            packageName = context.packageName
        )
    }
}

internal data class AppSettingsParts(
    val scheme: String,
    val packageName: String
)
