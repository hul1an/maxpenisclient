package com.github.hul1an.maxpenisclient.utils

import java.util.*

class TimeHelper {
    private var startTime: Long = System.currentTimeMillis()
    private var isPaused: Boolean = false
    private var pauseTime: Long? = null
    private var randomTime: Int = 0

    fun hasReached(time: Long): Boolean {
        return System.currentTimeMillis() - startTime > time
    }

    fun reset() {
        startTime = System.currentTimeMillis()
        isPaused = false
        pauseTime = null
    }

}