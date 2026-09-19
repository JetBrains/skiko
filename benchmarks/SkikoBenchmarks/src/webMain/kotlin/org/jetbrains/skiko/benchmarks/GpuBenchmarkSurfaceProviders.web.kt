package org.jetbrains.skiko.benchmarks

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.graphicapi.WebGLOffscreenContext

/**
 * Uses Skiko's WebGL backend for browser benchmarks.
 */
@OptIn(ExperimentalSkikoApi::class)
internal actual fun makeGpuBenchmarkSurfaceProvider(): BenchmarkSurfaceProvider? =
    WebGLBenchmarkSurfaceProvider()

/**
 * Creates benchmark surfaces from a hidden WebGL canvas render target.
 */
@OptIn(ExperimentalSkikoApi::class)
private class WebGLBenchmarkSurfaceProvider : BenchmarkSurfaceProvider {
    private val context = WebGLOffscreenContext()

    override fun withSurface(width: Int, height: Int, block: (Surface) -> Long): Long =
        context.withTexture(width, height) { texture ->
            val surface = Surface.makeFromBackendRenderTarget(
                context.directContext,
                texture.backendRenderTarget,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            ) ?: error("Cannot create WebGL benchmark surface")

            surface.use(block)
        }

    override fun close() {
        context.close()
    }
}
