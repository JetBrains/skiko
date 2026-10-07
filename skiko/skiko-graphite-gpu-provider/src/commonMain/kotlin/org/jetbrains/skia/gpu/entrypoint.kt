// Keep this file in sync with skiko-ganesh-gpu-provider's entrypoint.kt.
@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.Logger

fun makeMetalContext(devicePtr: NativePointer, queuePtr: NativePointer): GpuContext {
    return GpuContext(GraphiteGpuContextBackend.makeMetal(devicePtr, queuePtr)).also {
        Logger.info { "Metal backend: Graphite" }
    }
}
