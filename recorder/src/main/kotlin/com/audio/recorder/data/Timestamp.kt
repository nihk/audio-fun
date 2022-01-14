package com.audio.recorder.data

import javax.inject.Inject

interface Timestamp {
    fun current(): Long
}

class SystemTimestamp @Inject constructor() : Timestamp {
    override fun current(): Long {
        return System.currentTimeMillis()
    }
}
