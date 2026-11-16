@file:OptIn(
    ExperimentalForeignApi::class,
    ExperimentalSkikoApi::class,
)

package org.jetbrains.skia.gpu

import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.InternalSkikoApi

/**
 * Contract implemented by GPU provider modules to supply rendering operations to [GpuContext].
 *
 * Each provider manages its own native context and accepts its own [GpuTexture] implementation.
 */
@InternalSkikoApi
interface GpuContextBackend {
    fun makeSurface(
        texture: GpuTexture,
        colorSpace: ColorSpace?,
        surfaceProps: SurfaceProps?,
    ): Surface?

    fun submit(syncCpu: Boolean)
    fun close()
}

/**
 * Creates drawing surfaces and submits GPU work.
 *
 * Obtain an instance through a factory function supplied by the selected GPU provider module,
 * such as `makeMetalContext(devicePtr, queuePtr)`.
 *
 * The provider module supplies the native context implementation, allowing rendering code to use
 * this API without depending on a specific Skia backend. Textures passed to [makeSurface] must come
 * from the same provider and reference GPU resources compatible with this context.
 */
@ExperimentalSkikoApi
class GpuContext @InternalSkikoApi constructor(
    private val backend: GpuContextBackend,
) {
    private var closed = false

    /**
     * Wraps [texture] in a surface that the caller must close, or returns null if wrapping fails.
     * Keep this context, the texture, and its underlying GPU resource valid while using the surface.
     * Ownership of [texture] remains with the caller.
     *
     * @param colorSpace range of colors; may be null.
     * @param surfaceProps LCD striping orientation and setting for device independent fonts; may be null.
     * @throws IllegalArgumentException if [texture] belongs to another provider.
     * @throws IllegalStateException if this context or [texture] is closed.
     */
    fun makeSurface(
        texture: GpuTexture,
        colorSpace: ColorSpace? = ColorSpace.sRGB,
        surfaceProps: SurfaceProps? = SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN),
    ): Surface? {
        check(!closed) { "GPU context is closed" }
        return backend.makeSurface(texture, colorSpace, surfaceProps)
    }

    /**
     * Flushes pending drawing work and submits it to the GPU.
     *
     * @param syncCpu whether to wait for GPU execution to finish before returning.
     */
    fun submit(syncCpu: Boolean = false) {
        check(!closed) { "GPU context is closed" }
        backend.submit(syncCpu)
    }

    /**
     * Releases the provider's native context.
     */
    fun close() {
        if (!closed) {
            closed = true
            backend.close()
        }
    }
}
