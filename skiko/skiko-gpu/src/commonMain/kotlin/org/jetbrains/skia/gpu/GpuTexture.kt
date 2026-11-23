package org.jetbrains.skia.gpu

import org.jetbrains.skiko.ExperimentalSkikoApi

/**
 * Wraps a native GPU texture for use with [GpuContext.makeSurface].
 *
 * The selected GPU provider supplies the implementation. The texture must come from the same
 * provider as the context and reference a GPU resource compatible with that context.
 *
 * The caller owns this wrapper and must keep it and the underlying GPU resource valid while
 * surfaces or pending GPU work use them. Closing a context does not close its textures.
 */
@ExperimentalSkikoApi
interface GpuTexture {
    val width: Int
    val height: Int
    fun close()
}
