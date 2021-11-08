package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult

class SVGPreserveAspectRatio(align: SVGPreserveAspectRatioAlign, scale: SVGPreserveAspectRatioScale) {
    internal val _align: SVGPreserveAspectRatioAlign

    internal val _scale: SVGPreserveAspectRatioScale

    companion object {
        internal fun fromInterop(block: InteropScope.(InteropPointer) -> Unit) =
            withResult(IntArray(2), block).let {
                SVGPreserveAspectRatio(it[0], it[1])
            }
    }

    internal constructor(align: Int, scale: Int) : this(
        SVGPreserveAspectRatioAlign.valueOf(align),
        SVGPreserveAspectRatioScale.values()[scale]
    )

    constructor() : this(SVGPreserveAspectRatioAlign.XMID_YMID, SVGPreserveAspectRatioScale.MEET)
    constructor(align: SVGPreserveAspectRatioAlign) : this(align, SVGPreserveAspectRatioScale.MEET)
    constructor(scale: SVGPreserveAspectRatioScale) : this(SVGPreserveAspectRatioAlign.XMID_YMID, scale)

    val align: SVGPreserveAspectRatioAlign
        get() = _align
    val scale: SVGPreserveAspectRatioScale
        get() = _scale

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
        return "SVGPreserveAspectRatio(_align=$align, _scale=$scale)"
    }

    fun withAlign(_align: SVGPreserveAspectRatioAlign): SVGPreserveAspectRatio {
        return if (this._align === _align) this else SVGPreserveAspectRatio(_align, _scale)
    }

    fun withScale(_scale: SVGPreserveAspectRatioScale): SVGPreserveAspectRatio {
        return if (this._scale === _scale) this else SVGPreserveAspectRatio(_align, _scale)
    }

    init {
        _align = align
        _scale = scale
    }
}