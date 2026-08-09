@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.jetbrains.skiko

import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.impl.NativePointer

/**
 * Platform host used by the Kotlin/Native Linux [SkiaLayer].
 *
 * Skiko owns the Skia and Ganesh objects while the embedding toolkit owns the native window and
 * its event loop. Implementations must invoke [requestRender] on the window thread.
 */
@InternalSkikoApi
interface LinuxSkiaLayerComponent {
    /** Native window object exposed through [SkiaLayer.component]. */
    val windowHandle: Any

    /** Current drawable size in physical pixels. */
    val drawableWidth: Int
    val drawableHeight: Int

    /** Scale from logical coordinates to physical pixels. */
    val contentScale: Float

    val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN

    /** Refresh rate of the display containing this window. */
    val displayRefreshRate: Float
        get() = 60f

    var fullscreen: Boolean

    val transparency: Boolean
        get() = false

    /** Whether the host created a window buffer whose alpha channel reaches the window system. */
    val transparencySupported: Boolean
        get() = false

    /** Effective number of buffers exposed by the presentation API, or null when it is unknown. */
    val effectiveFrameBufferCount: Int?
        get() = null

    /** Context passed to [openGlResolver]. */
    val openGlResolverContext: NativePointer

    /** Native `GrGLGetProc` callback used to assemble Skia's OpenGL function table. */
    val openGlResolver: NativePointer

    fun createOpenGlContext(): NativePointer

    fun makeOpenGlContextCurrent(context: NativePointer)

    /** Returns whether the requested presentation interval was accepted. */
    fun setOpenGlSwapInterval(interval: Int): Boolean

    fun swapOpenGlBuffers()

    fun deleteOpenGlContext(context: NativePointer)

    /** Name of the adapter backing the current OpenGL context, when available. */
    fun openGlRendererName(): String? = null

    /** Returns true after the platform reports that the current OpenGL context was lost. */
    fun isOpenGlContextLost(context: NativePointer): Boolean = false

    /** Applies a platform-specific GPU preference before a context is created. */
    fun configureGpuPriority(priority: GpuPriority) = Unit

    /** Applies framebuffer configuration before a context is created. */
    fun configureFrameBuffering(frameBuffering: FrameBuffering) = Unit

    /** Prepares the host to present Skia raster surfaces. */
    fun beginSoftwareRendering() = Unit

    /** Presents premultiplied native N32 pixels to the window. */
    fun presentSoftwareFrame(
        pixels: NativePointer,
        width: Int,
        height: Int,
        rowBytes: Int,
    ) {
        error("The Linux SkiaLayer host does not support software presentation")
    }

    /** Releases resources allocated by [beginSoftwareRendering]. */
    fun endSoftwareRendering() = Unit

    /** Wakes the embedding toolkit's event loop so it can call [SkiaLayer.render]. */
    fun requestRender()
}
