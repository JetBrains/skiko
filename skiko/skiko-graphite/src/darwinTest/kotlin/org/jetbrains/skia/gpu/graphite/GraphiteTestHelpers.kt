package org.jetbrains.skia.gpu.graphite

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.objcPtr
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import platform.Metal.MTLCreateSystemDefaultDevice

@OptIn(ExperimentalForeignApi::class, ExperimentalSkikoApi::class)
internal actual fun makeTestGraphiteContext(): GraphiteContext? {
    if (hostOs == OS.Tvos) return null

    val device = checkNotNull(MTLCreateSystemDefaultDevice()) {
        "Metal is not supported on this system"
    }
    val queue = checkNotNull(device.newCommandQueue()) {
        "Failed to create a Metal command queue"
    }
    return GraphiteContext.makeMetal(device.objcPtr(), queue.objcPtr())
}
