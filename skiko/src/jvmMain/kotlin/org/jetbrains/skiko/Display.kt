package org.jetbrains.skiko

import kotlin.time.ExperimentalTime

internal const val MinMainstreamMonitorRefreshRate = 60.0

@OptIn(ExperimentalTime::class)
internal fun HardwareLayer.getDisplayRefreshRate(): Double {
    // We use different method for Linux, because it.displayMode.refreshRate returns always a wrong value: 50 (probably because of the using the old xrandr API)
    return if (hostOs == OS.Linux) {
        lockLinuxDrawingSurface {
            getLinuxDisplayRefreshRate(it.display, it.window)
        }
    } else {
        graphicsConfiguration
            .device
            .displayMode
            .refreshRate
            .toDouble()
            .coerceAtLeast(MinMainstreamMonitorRefreshRate)
    }
}

private external fun getLinuxDisplayRefreshRate(display: Long, window: Long): Double