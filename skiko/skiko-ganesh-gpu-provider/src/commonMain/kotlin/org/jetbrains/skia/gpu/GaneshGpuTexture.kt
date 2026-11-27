@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin

internal class GaneshGpuTexture(
    override val width: Int,
    override val height: Int,
    val renderTarget: BackendRenderTarget,
    val origin: SurfaceOrigin = SurfaceOrigin.TOP_LEFT,
    val colorFormat: SurfaceColorFormat = SurfaceColorFormat.BGRA_8888,
) : GpuTexture {
    val isClosed: Boolean get() = renderTarget.isClosed

    override fun close() {
        if (!isClosed) renderTarget.close()
    }
}
