package com.github.hul1an.maxpenisclient.clock

class TimeHelper {
    private var startTime: Long = System.currentTimeMillis()
    private var isPaused: Boolean = false
    private var pauseTime: Long? = null
    private var randomTime: Long = 0

    fun hasReached(time: Long): Boolean {
        return System.currentTimeMillis() - startTime > time
    }

    fun setHasReached() {
        startTime = 0
    }

    fun reset() {
        startTime = System.currentTimeMillis()
        isPaused = false
        pauseTime = null
    }

    fun getTime(): Long {
        return startTime
    }

    fun setTime(time: Long) {
        startTime = time
    }

    fun getTimePassed(): Long {
        val currentTime = System.currentTimeMillis()
        var dt = currentTime - startTime
        if (isPaused) dt += currentTime - (pauseTime ?: 0)
        return dt
    }

    fun pause() {
        if (!isPaused) {
            isPaused = true
            pauseTime = System.currentTimeMillis()
        }
    }

    fun unpause() {
        if (isPaused) {
            isPaused = false
            startTime += System.currentTimeMillis() - (pauseTime ?: 0)
            pauseTime = null
        }
    }

    fun setRandomReached(min: Long, max: Long) {
        randomTime = max - (Math.random() * min).toLong()
    }

    fun reachedRandom(): Boolean {
        return System.currentTimeMillis() - startTime > randomTime
    }
}