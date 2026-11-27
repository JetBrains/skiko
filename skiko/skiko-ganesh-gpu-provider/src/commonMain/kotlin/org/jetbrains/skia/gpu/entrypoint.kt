// Keep the API in this file in sync with skiko-graphite-gpu-provider's entrypoint.kt.
package org.jetbrains.skia.gpu

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.internal.Logger

@ExperimentalSkikoApi
fun makeMetalContext(devicePtr: NativePointer, queuePtr: NativePointer): GpuContext {
    Logger.debug { "Metal backend: Ganesh" }
    return GpuContext(GaneshGpuContextBackend.makeMetal(devicePtr, queuePtr))
}

@ExperimentalSkikoApi
fun makeMetalTexture(width: Int, height: Int, texturePtr: NativePointer): GpuTexture =
    GaneshGpuTexture(width, height, BackendRenderTarget.makeMetal(width, height, texturePtr))

/**
 * Creates a context on top of the OpenGL context that is current on the calling thread.
 */
@ExperimentalSkikoApi
fun makeOpenGLContext(): GpuContext {
    Logger.debug { "OpenGL backend: Ganesh" }
    return GpuContext(GaneshGpuContextBackend.makeGL())
}

/**
 * Wraps an OpenGL framebuffer (e.g. `GL_DRAW_FRAMEBUFFER_BINDING`) as a render target.
 * The resulting surface is bottom-left origin and RGBA_8888.
 */
@ExperimentalSkikoApi
fun makeOpenGLTexture(
    width: Int,
    height: Int,
    sampleCount: Int,
    stencilBits: Int,
    framebufferId: Int,
    framebufferFormat: Int,
): GpuTexture = GaneshGpuTexture(
    width,
    height,
    BackendRenderTarget.makeGL(width, height, sampleCount, stencilBits, framebufferId, framebufferFormat),
    origin = SurfaceOrigin.BOTTOM_LEFT,
    colorFormat = SurfaceColorFormat.RGBA_8888,
)
