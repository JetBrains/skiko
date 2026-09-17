@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.InternalSkikoApi

@InternalSkikoApi
interface GpuContextBackend {
    fun makeSurface(
        width: Int,
        height: Int,
        texturePtr: NativePointer,
        colorSpace: ColorSpace?,
        surfaceProps: SurfaceProps?,
    ): Surface?

    fun submit(syncCpu: Boolean)
    fun close()
}

/** A Skia GPU context supplied by the selected GPU backend. */
@ExperimentalSkikoApi
class GpuContext @InternalSkikoApi constructor(
    private val backend: GpuContextBackend,
) {
    private var closed = false

    fun makeSurface(
        width: Int,
        height: Int,
        texturePtr: NativePointer,
        colorSpace: ColorSpace? = ColorSpace.sRGB,
        surfaceProps: SurfaceProps? = SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN),
    ): Surface? {
        check(!closed) { "GPU context is closed" }
        return backend.makeSurface(width, height, texturePtr, colorSpace, surfaceProps)
    }

    fun submit(syncCpu: Boolean = false) {
        check(!closed) { "GPU context is closed" }
        backend.submit(syncCpu)
    }

    fun close() {
        if (!closed) {
            closed = true
            backend.close()
        }
    }
}