package org.jetbrains.skia.encode

import org.jetbrains.skia.Data
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.*

/** Options for Skia's PNG encoder. */
data class PngEncoder(
    val compressionLevel: Int = 6,
    val filters: Set<Filter> = Filter.entries.toSet(),
) : ImageEncoder {
    enum class Filter(internal val flag: Int) {
        NONE(0x08), SUB(0x10), UP(0x20), AVG(0x40), PAETH(0x80)
    }

    init {
        require(compressionLevel in 0..9) { "PNG compression level must be in 0..9" }
        require(filters.isNotEmpty()) { "PNG filters must not be empty" }
    }

    override fun encode(image: Image): Data? = try {
        Stats.onNativeCall()
        val flags = filters.fold(0) { mask, filter -> mask or filter.flag }
        val ptr = _nEncodePng(image._ptr, compressionLevel, flags)
        if (ptr == Native.NullPointer) null else Data(ptr)
    } finally {
        reachabilityBarrier(image)
    }
}

@ExternalSymbolName("org_jetbrains_skia_encode_PngEncoder__1nEncodePng")
private external fun _nEncodePng(ptr: NativePointer, compressionLevel: Int, filterFlags: Int): NativePointer
