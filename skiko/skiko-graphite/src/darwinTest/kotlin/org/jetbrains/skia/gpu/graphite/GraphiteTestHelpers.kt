package org.jetbrains.skia.gpu.graphite

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.objcPtr
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import platform.Metal.MTLCreateSystemDefaultDevice

@OptIn(ExperimentalForeignApi::class, ExperimentalSkikoApi::class)
internal actual fun withTestGraphiteContext(block: (GraphiteContext) -> Unit) {
    if (hostOs == OS.Tvos) return

    makeTestMetalContext().use(block)
}

@OptIn(ExperimentalForeignApi::class, ExperimentalSkikoApi::class)
private fun makeTestMetalContext(): GraphiteContext {
    val device = checkNotNull(MTLCreateSystemDefaultDevice()) {
        "Metal is not supported on this system"
    }
    val queue = checkNotNull(device.newCommandQueue()) {
        "Failed to create a Metal command queue"
    }
    return GraphiteContext.makeMetal(device.objcPtr(), queue.objcPtr())
}
