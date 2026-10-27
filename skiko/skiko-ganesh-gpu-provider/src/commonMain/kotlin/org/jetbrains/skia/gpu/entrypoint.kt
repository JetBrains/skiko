// Keep the API in this file in sync with skiko-graphite-gpu-provider's entrypoint.kt.
package org.jetbrains.skia.gpu

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.Logger

@ExperimentalSkikoApi
fun makeMetalContext(devicePtr: NativePointer, queuePtr: NativePointer): GpuContext {
    Logger.info { "Metal backend: Ganesh" }
    return GpuContext(GaneshGpuContextBackend.makeMetal(devicePtr, queuePtr))
}
