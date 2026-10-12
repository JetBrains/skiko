package org.jetbrains.skia.encode

import org.jetbrains.skia.Data
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.*

/** Options for Skia's WebP encoder. In lossless mode, quality controls encoding effort. */
data class WebpEncoder(
    val compression: Compression = Compression.LOSSY,
    val quality: Float = 100f,
) : ImageEncoder {
    enum class Compression(internal val nativeValue: Int) {
        LOSSY(0), LOSSLESS(1)
    }

    init {
        require(quality.isFinite() && quality in 0f..100f) { "WebP quality must be in 0..100" }
    }

    override fun encode(image: Image): Data? = try {
        Stats.onNativeCall()
        val ptr = _nEncodeWebp(image._ptr, compression.nativeValue, quality)
        if (ptr == Native.NullPointer) null else Data(ptr)
    } finally {
        reachabilityBarrier(image)
    }
}

@ExternalSymbolName("org_jetbrains_skia_encode_WebpEncoder__1nEncodeWebp")
private external fun _nEncodeWebp(ptr: NativePointer, compression: Int, quality: Float): NativePointer
