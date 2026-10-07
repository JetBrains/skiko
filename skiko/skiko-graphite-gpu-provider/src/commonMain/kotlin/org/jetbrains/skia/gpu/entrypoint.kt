// Keep this file in sync with skiko-ganesh-gpu-provider's entrypoint.kt.
@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.gpu.graphite.GraphiteContext
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.Logger

fun makeMetalContext(devicePtr: NativePointer, queuePtr: NativePointer): GpuContext {
    val context = GraphiteContext.makeMetal(devicePtr, queuePtr)
    return try {
        GpuContext(GraphiteMetalContext(context, context.makeRecorder())).also {
            Logger.info { "Metal backend: Graphite" }
        }
    } catch (error: Throwable) {
        context.close()
        throw error
    }
}
