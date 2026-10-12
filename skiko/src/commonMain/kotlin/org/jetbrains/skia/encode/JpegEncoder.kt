package org.jetbrains.skia.encode

import org.jetbrains.skia.Data
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.*

/** Options for Skia's JPEG encoder. */
data class JpegEncoder(
    val quality: Int = 100,
    val downsample: Downsample = Downsample.YUV_420,
    val alphaOption: AlphaOption = AlphaOption.IGNORE,
) : ImageEncoder {
    enum class Downsample(internal val nativeValue: Int) {
        YUV_420(0), YUV_422(1), YUV_444(2)
    }

    enum class AlphaOption(internal val nativeValue: Int) {
        IGNORE(0), BLEND_ON_BLACK(1)
    }

    init {
        require(quality in 0..100) { "JPEG quality must be in 0..100" }
    }

    override fun encode(image: Image): Data? = try {
        Stats.onNativeCall()
        val ptr = _nEncodeJpeg(image._ptr, quality, downsample.nativeValue, alphaOption.nativeValue)
        if (ptr == Native.NullPointer) null else Data(ptr)
    } finally {
        reachabilityBarrier(image)
    }
}

@ExternalSymbolName("org_jetbrains_skia_encode_JpegEncoder__1nEncodeJpeg")
private external fun _nEncodeJpeg(ptr: NativePointer, quality: Int, downsample: Int, alphaOption: Int): NativePointer
