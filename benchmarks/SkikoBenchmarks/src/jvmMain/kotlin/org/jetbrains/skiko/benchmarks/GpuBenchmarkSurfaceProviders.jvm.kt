package org.jetbrains.skiko.benchmarks

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.graphicapi.DirectXOffscreenContext
import org.jetbrains.skiko.graphicapi.MetalOffscreenContext
import org.jetbrains.skiko.graphicapi.OpenGLOffscreenContext
import org.jetbrains.skiko.hostOs

/**
 * Selects the native GPU backend used by Skiko on each JVM host platform.
 */
internal actual fun makeGpuBenchmarkSurfaceProvider(): BenchmarkSurfaceProvider? {
    return try {
        when (hostOs) {
            OS.Windows -> DirectXBenchmarkSurfaceProvider()
            OS.MacOS -> MetalBenchmarkSurfaceProvider()
            OS.Linux -> OpenGLBenchmarkSurfaceProvider()
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Creates benchmark surfaces from offscreen DirectX render targets on Windows.
 */
@OptIn(ExperimentalSkikoApi::class)
private class DirectXBenchmarkSurfaceProvider : BenchmarkSurfaceProvider {
    private val context = DirectXOffscreenContext()

    override fun withSurface(width: Int, height: Int, block: (Surface) -> Long): Long {
        val texture = context.Texture(width, height)
        return texture.use {
            val surface = Surface.makeFromBackendRenderTarget(
                context.directContext,
                texture.backendRenderTarget,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            ) ?: error("Cannot create DirectX benchmark surface")

            surface.use(block)
        }
    }

    override fun close() {
        context.close()
    }
}

/**
 * Creates benchmark surfaces from offscreen Metal render targets on macOS.
 */
@OptIn(ExperimentalSkikoApi::class)
private class MetalBenchmarkSurfaceProvider : BenchmarkSurfaceProvider {
    private val context = MetalOffscreenContext()

    override fun withSurface(width: Int, height: Int, block: (Surface) -> Long): Long {
        val texture = context.Texture(width, height)
        return texture.use {
            val surface = Surface.makeFromBackendRenderTarget(
                context.directContext,
                texture.backendRenderTarget,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            ) ?: error("Cannot create Metal benchmark surface")

            surface.use(block)
        }
    }

    override fun close() {
        context.close()
    }
}

/**
 * Creates benchmark surfaces from offscreen OpenGL render targets on Linux.
 */
@OptIn(ExperimentalSkikoApi::class)
private class OpenGLBenchmarkSurfaceProvider : BenchmarkSurfaceProvider {
    private val context = OpenGLOffscreenContext()

    override fun withSurface(width: Int, height: Int, block: (Surface) -> Long): Long =
        context.withTexture(width, height) { texture ->
            val surface = Surface.makeFromBackendRenderTarget(
                context.directContext,
                texture.backendRenderTarget,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN)
            ) ?: error("Cannot create OpenGL benchmark surface")

            surface.use(block)
        }

    override fun close() {
        context.close()
    }
}
