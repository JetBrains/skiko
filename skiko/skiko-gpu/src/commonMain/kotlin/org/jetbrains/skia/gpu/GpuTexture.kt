package org.jetbrains.skia.gpu

import org.jetbrains.skiko.ExperimentalSkikoApi

@ExperimentalSkikoApi
interface GpuTexture {
    val width: Int
    val height: Int
    fun close()
}
