package org.jetbrains.skiko

import java.awt.GraphicsEnvironment

internal const val MinMainstreamMonitorRefreshRate = 60.0

internal fun getMaxDisplayRefreshRate(): Double {
    return if (hostOs == OS.Linux) {
        // TODO it is difficult to retrieve all displays (https://stackoverflow.com/questions/11367354/obtaining-list-of-all-xorg-displays).
        //  So we need to switch to "one window - one FrameLimiter" approach, when we find time to do it (see FrameLimiter.kt).
//      we use different method for Linux, because it.displayMode.refreshRate returns always a wrong value: 50 (probably because of the using the old xrandr API)
        getLinuxDefaultDisplayRefreshRate()
    } else {
        GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .screenDevices
            .maxOf { it.displayMode.refreshRate }
            .toDouble()
            .coerceAtLeast(MinMainstreamMonitorRefreshRate)
    }
}

private external fun getLinuxDefaultDisplayRefreshRate(): Double