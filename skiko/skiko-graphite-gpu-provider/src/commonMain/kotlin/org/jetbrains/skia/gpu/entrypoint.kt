// Keep the API in this file in sync with skiko-ganesh-gpu-provider's entrypoint.kt.
package org.jetbrains.skia.gpu

import org.jetbrains.skia.gpu.graphite.BackendTexture
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.Logger

@ExperimentalSkikoApi
fun makeMetalContext(devicePtr: NativePointer, queuePtr: NativePointer): GpuContext {
    Logger.info { "Metal backend: Graphite" }
    return GpuContext(GraphiteGpuContextBackend.makeMetal(devicePtr, queuePtr))
}

@ExperimentalSkikoApi
fun makeMetalTexture(width: Int, height: Int, texturePtr: NativePointer): GpuTexture =
    GraphiteGpuTexture(width, height, BackendTexture.makeMetal(width, height, texturePtr))
