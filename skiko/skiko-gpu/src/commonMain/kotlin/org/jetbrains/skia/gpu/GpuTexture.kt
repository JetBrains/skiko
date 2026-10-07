package org.jetbrains.skia.gpu

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi

@ExperimentalSkikoApi
sealed interface GpuTexture {
    val width: Int
    val height: Int
}

@ExperimentalSkikoApi
class MetalGpuTexture(
    override val width: Int,
    override val height: Int,
    val texturePtr: NativePointer,
) : GpuTexture
