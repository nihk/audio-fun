package com.audio.files

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Filename(
    val simple: String,
    val absolute: String,
    val extension: String
) : Parcelable
