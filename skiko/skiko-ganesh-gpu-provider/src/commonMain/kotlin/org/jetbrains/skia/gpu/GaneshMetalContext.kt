@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.gpu.ganesh.makeFromBackendRenderTarget
import org.jetbrains.skia.impl.NativePointer

internal class GaneshMetalContext(private val context: DirectContext) : GpuContextBackend {
    override fun makeSurface(width: Int, height: Int, texturePtr: NativePointer, colorSpace: ColorSpace?, surfaceProps: SurfaceProps?): Surface? {
        val renderTarget = BackendRenderTarget.makeMetal(width, height, texturePtr)
        val surface = Surface.makeFromBackendRenderTarget(context, renderTarget, SurfaceOrigin.TOP_LEFT, SurfaceColorFormat.BGRA_8888, colorSpace, surfaceProps)
        if (surface == null) {
            renderTarget.close()
        }
        return surface
    }

    override fun submit(syncCpu: Boolean) = context.flush().submit(syncCpu)
    override fun close() = context.close()
}
