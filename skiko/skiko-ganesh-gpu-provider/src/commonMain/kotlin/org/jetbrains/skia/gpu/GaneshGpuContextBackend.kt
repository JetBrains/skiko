@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.gpu.ganesh.makeFromBackendRenderTarget
import org.jetbrains.skia.impl.NativePointer

internal class GaneshGpuContextBackend private constructor(
    private val context: DirectContext,
) : GpuContextBackend {
    companion object {
        fun makeMetal(
            devicePtr: NativePointer,
            queuePtr: NativePointer,
        ) = GaneshGpuContextBackend(DirectContext.makeMetal(devicePtr, queuePtr))
    }

    override fun makeSurface(
        texture: GpuTexture,
        colorSpace: ColorSpace?,
        surfaceProps: SurfaceProps?,
    ): Surface? {
        require(texture is GaneshGpuTexture) { "Texture was not created by the Ganesh provider" }
        val renderTarget = texture.renderTarget
        check(!renderTarget.isClosed) { "GPU texture is closed" }
        return Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            colorSpace,
            surfaceProps,
        )
    }

    override fun submit(syncCpu: Boolean) = context.flush().submit(syncCpu)

    override fun close() = context.close()
}
