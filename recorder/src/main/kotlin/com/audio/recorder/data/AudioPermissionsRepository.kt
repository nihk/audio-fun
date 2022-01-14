package com.audio.recorder.data

import android.Manifest
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AudioPermissionsRepository {
    fun permission(): String
    fun appSettingsParts(): AppSettingsParts
}

class AndroidAudioPermissionsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPermissionsRepository {
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

data class AppSettingsParts(
    val scheme: String,
    val packageName: String
)
