package com.audio.recorder.data

internal interface Timestamp {
    fun current(): Long
}

internal class SystemTimestamp : Timestamp {
    override fun current(): Long {
        return System.currentTimeMillis()
    }
}
