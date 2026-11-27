// Keep the API in this file in sync with skiko-ganesh-gpu-provider's entrypoint.kt.
package org.jetbrains.skia.gpu

import org.jetbrains.skia.gpu.graphite.BackendTexture
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.internal.Logger

@ExperimentalSkikoApi
fun makeMetalContext(devicePtr: NativePointer, queuePtr: NativePointer): GpuContext {
    Logger.debug { "Metal backend: Graphite" }
    return GpuContext(GraphiteGpuContextBackend.makeMetal(devicePtr, queuePtr))
}

@ExperimentalSkikoApi
fun makeMetalTexture(width: Int, height: Int, texturePtr: NativePointer): GpuTexture =
    GraphiteGpuTexture(width, height, BackendTexture.makeMetal(width, height, texturePtr))

@ExperimentalSkikoApi
fun makeOpenGLContext(): GpuContext =
    throw UnsupportedOperationException("OpenGL is not supported by the Graphite provider")

@ExperimentalSkikoApi
fun makeOpenGLTexture(
    width: Int,
    height: Int,
    sampleCount: Int,
    stencilBits: Int,
    framebufferId: Int,
    framebufferFormat: Int,
): GpuTexture =
    throw UnsupportedOperationException("OpenGL is not supported by the Graphite provider")
