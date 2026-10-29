package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult
import kotlin.jvm.JvmStatic

class SVGPreserveAspectRatio(val align: SVGPreserveAspectRatioAlign, val scale: SVGPreserveAspectRatioScale) {

    companion object {
        @JvmStatic
        internal fun fromInterop(block: InteropScope.(InteropPointer) -> Unit) =
            withResult(IntArray(2), block).let {
                SVGPreserveAspectRatio(it[0], it[1])
            }
    }

    internal constructor(align: Int, scale: Int) : this(
        SVGPreserveAspectRatioAlign.valueOf(align),
        SVGPreserveAspectRatioScale.entries[scale]
    )

    constructor() : this(SVGPreserveAspectRatioAlign.XMID_YMID, SVGPreserveAspectRatioScale.MEET)
    constructor(align: SVGPreserveAspectRatioAlign) : this(align, SVGPreserveAspectRatioScale.MEET)
    constructor(scale: SVGPreserveAspectRatioScale) : this(SVGPreserveAspectRatioAlign.XMID_YMID, scale)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SVGPreserveAspectRatio) return false
        if (align != other.align) return false
        return scale == other.scale
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + align.hashCode()
        result = result * PRIME + scale.hashCode()
        return result
    }

    override fun toString(): String {
        return "SVGPreserveAspectRatio(align=$align, scale=$scale)"
    }

    fun withAlign(_align: SVGPreserveAspectRatioAlign): SVGPreserveAspectRatio {
        return if (this.align === _align) this else SVGPreserveAspectRatio(_align, scale)
    }

    fun withScale(_scale: SVGPreserveAspectRatioScale): SVGPreserveAspectRatio {
        return if (this.scale === _scale) this else SVGPreserveAspectRatio(align, _scale)
    }
}