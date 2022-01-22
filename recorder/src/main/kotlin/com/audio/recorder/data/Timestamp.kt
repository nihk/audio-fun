package com.audio.recorder.data

import javax.inject.Inject

internal interface Timestamp {
    fun current(): Long
}

internal class SystemTimestamp @Inject constructor() : Timestamp {
    override fun current(): Long {
        return System.currentTimeMillis()
    }
}
