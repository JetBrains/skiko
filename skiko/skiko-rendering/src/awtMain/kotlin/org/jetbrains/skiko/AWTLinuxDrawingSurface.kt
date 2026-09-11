package org.jetbrains.skiko

internal inline fun <T> HardwareLayer.lockLinuxDrawingSurface(action: (LinuxDrawingSurface) -> T): T {
    val drawingSurface = lockLinuxDrawingSurface(this)
    try {
        return action(drawingSurface)
    } finally {
        unlockLinuxDrawingSurface(drawingSurface)
    }
}

internal fun lockLinuxDrawingSurface(layer: HardwareLayer): LinuxDrawingSurface {
    val drawingSurface = layer.getDrawingSurface()
    drawingSurface.lock()
    return drawingSurface.getInfo().use {
        LinuxDrawingSurface(
            drawingSurface,
            getDisplay(it.platformInfo),
            getWindow(it.platformInfo)
        )
    }
}

internal fun unlockLinuxDrawingSurface(drawingSurface: LinuxDrawingSurface) {
    drawingSurface.common.unlock()
    drawingSurface.common.close()
}

internal class LinuxDrawingSurface(
    val common: DrawingSurface,
    val display: Long,
    val window: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LinuxDrawingSurface

        if (display != other.display) return false
        if (window != other.window) return false

        return true
    }

    override fun hashCode(): Int {
        var result = display.hashCode()
        result = 31 * result + window.hashCode()
        return result
    }
}

private external fun getDisplay(platformInfo: Long): Long
private external fun getWindow(platformInfo: Long): Long
