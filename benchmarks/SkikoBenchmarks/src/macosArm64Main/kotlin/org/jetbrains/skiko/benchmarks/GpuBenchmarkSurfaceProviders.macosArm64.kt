@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package org.jetbrains.skiko.benchmarks

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.objcPtr
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.gpu.ganesh.makeFromBackendRenderTarget
import org.jetbrains.skia.impl.use
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLPixelFormatBGRA8Unorm
import platform.Metal.MTLTextureDescriptor
import platform.Metal.MTLTextureType2D
import platform.Metal.MTLTextureUsageRenderTarget
import platform.Metal.MTLTextureUsageShaderRead
import platform.Metal.MTLTextureUsageShaderWrite

internal actual fun makeGpuBenchmarkSurfaceProvider(): BenchmarkSurfaceProvider? =
    try {
        MetalBenchmarkSurfaceProvider()
    } catch (_: Exception) {
        null
    }

/**
 * Creates benchmark surfaces from offscreen Metal render targets on macOS.
 */
private class MetalBenchmarkSurfaceProvider : BenchmarkSurfaceProvider {
    private val device = MTLCreateSystemDefaultDevice() ?: error("Can't create MTLDevice")
    private val commandQueue = device.newCommandQueue() ?: error("Can't create MTLCommandQueue")
    private val directContext = DirectContext.makeMetal(device.objcPtr(), commandQueue.objcPtr())

    override fun withSurface(width: Int, height: Int, block: (Surface) -> Long): Long = autoreleasepool {
        val descriptor = MTLTextureDescriptor()
        descriptor.width = width.toULong()
        descriptor.height = height.toULong()
        descriptor.usage = MTLTextureUsageShaderRead or MTLTextureUsageShaderWrite or MTLTextureUsageRenderTarget
        descriptor.textureType = MTLTextureType2D
        descriptor.pixelFormat = MTLPixelFormatBGRA8Unorm
        descriptor.mipmapLevelCount = 1UL

        val texture = device.newTextureWithDescriptor(descriptor) ?: error("Can't create MTLTexture")

        BackendRenderTarget.makeMetal(width, height, texture.objcPtr()).use { renderTarget ->
            val surface = Surface.makeFromBackendRenderTarget(
                directContext,
                renderTarget,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            ) ?: error("Cannot create Metal benchmark surface")

            surface.use(block)
        }
    }

    override fun close() {
        directContext.close()
    }
}
