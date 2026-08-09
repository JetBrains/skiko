@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.jetbrains.skiko

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.nanosleep
import platform.posix.timespec

internal class LinuxFrameLimiter {
    private var nextFrameNanos = 0L

    fun awaitNextFrame(refreshRate: Float) {
        val safeRefreshRate = refreshRate.takeIf { it.isFinite() && it > 1f } ?: 60f
        val interval = (1_000_000_000.0 / safeRefreshRate).toLong()
        val now = currentNanoTime()
        if (nextFrameNanos > now) sleepNanos(nextFrameNanos - now)
        val afterWait = currentNanoTime()
        nextFrameNanos = maxOf(nextFrameNanos + interval, afterWait + interval)
    }

    private fun sleepNanos(duration: Long) {
        if (duration <= 0L) return
        memScoped {
            val request = alloc<timespec>()
            request.tv_sec = duration / 1_000_000_000L
            request.tv_nsec = duration % 1_000_000_000L
            nanosleep(request.ptr, null)
        }
    }
}
