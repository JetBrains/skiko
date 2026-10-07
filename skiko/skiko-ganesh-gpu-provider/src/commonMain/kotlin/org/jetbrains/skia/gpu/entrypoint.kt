// Keep this file in sync with skiko-graphite-gpu-provider's entrypoint.kt.
@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.Logger

fun makeMetalContext(devicePtr: NativePointer, queuePtr: NativePointer): GpuContext {
    Logger.info { "Metal backend: Ganesh" }
    return GpuContext(GaneshGpuContextBackend.makeMetal(devicePtr, queuePtr))
}
