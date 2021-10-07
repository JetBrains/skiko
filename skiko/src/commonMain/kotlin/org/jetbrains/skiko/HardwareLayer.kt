package org.jetbrains.skiko

internal expect open class HardwareLayer {
    open fun init()
    open fun dispose()
}