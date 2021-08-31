package org.jetbrains.skija.svg

import org.jetbrains.annotations.ApiStatus

class SVGPreserveAspectRatio(align: SVGPreserveAspectRatioAlign, scale: SVGPreserveAspectRatioScale) {
    @ApiStatus.Internal
    val _align: SVGPreserveAspectRatioAlign

    @ApiStatus.Internal
    val _scale: SVGPreserveAspectRatioScale

    @ApiStatus.Internal
    constructor(align: Int, scale: Int) : this(
        SVGPreserveAspectRatioAlign.valueOf(align),
        SVGPreserveAspectRatioScale._values.get(scale)
    )

    constructor() : this(SVGPreserveAspectRatioAlign.XMID_YMID, SVGPreserveAspectRatioScale.MEET) {}
    constructor(align: SVGPreserveAspectRatioAlign) : this(align, SVGPreserveAspectRatioScale.MEET) {}
    constructor(scale: SVGPreserveAspectRatioScale) : this(SVGPreserveAspectRatioAlign.XMID_YMID, scale) {}

    val align: SVGPreserveAspectRatioAlign
        get() = _align
    val scale: SVGPreserveAspectRatioScale
        get() = _scale

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is SVGPreserveAspectRatio) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_align`: Any = align
        val `other$_align`: Any = other.align
        if (if (`this$_align` == null) `other$_align` != null else `this$_align` != `other$_align`) return false
        val `this$_scale`: Any = scale
        val `other$_scale`: Any = other.scale
        return if (if (`this$_scale` == null) `other$_scale` != null else `this$_scale` != `other$_scale`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is SVGPreserveAspectRatio
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_align`: Any = align
        result = result * PRIME + (`$_align`?.hashCode() ?: 43)
        val `$_scale`: Any = scale
        result = result * PRIME + (`$_scale`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "SVGPreserveAspectRatio(_align=" + align + ", _scale=" + scale + ")"
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