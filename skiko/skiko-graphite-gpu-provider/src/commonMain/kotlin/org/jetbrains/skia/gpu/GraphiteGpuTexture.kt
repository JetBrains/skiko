@file:OptIn(org.jetbrains.skiko.ExperimentalSkikoApi::class)

package org.jetbrains.skia.gpu

import org.jetbrains.skia.gpu.graphite.BackendTexture

internal class GraphiteGpuTexture(
    override val width: Int,
    override val height: Int,
    val backendTexture: BackendTexture,
) : GpuTexture {
    val isClosed: Boolean get() = backendTexture.isClosed

    override fun close() {
        if (!isClosed) backendTexture.close()
    }
}
