@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.BackendRenderTarget

internal class GaneshGpuTexture(
    override val width: Int,
    override val height: Int,
    val renderTarget: BackendRenderTarget,
) : GpuTexture {
    val isClosed: Boolean get() = renderTarget.isClosed

    override fun close() {
        if (!isClosed) renderTarget.close()
    }
}
