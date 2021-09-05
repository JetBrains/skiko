package org.jetbrains.skia

/**
 *
 * Describes pixel and encoding. ImageInfo can be created from ColorInfo by
 * providing dimensions.
 *
 *
 * It encodes how pixel bits describe alpha, transparency; color components red, blue,
 * and green; and ColorSpace, the range and linearity of colors.
 */
class ColorInfo(val colorType: ColorType, val alphaType: ColorAlphaType, val colorSpace: ColorSpace?) {
    val isOpaque: Boolean
        get() = alphaType == ColorAlphaType.OPAQUE || colorType.isAlwaysOpaque

    /**
     * Returns number of bytes per pixel required by ColorType.
     * Returns zero if getColorType() is [ColorType.UNKNOWN].
     *
     * @return  bytes in pixel
     *
     * @see [https://fiddle.skia.org/c/@ImageInfo_bytesPerPixel](https://fiddle.skia.org/c/@ImageInfo_bytesPerPixel)
     */
    val bytesPerPixel: Int
        get() = colorType.bytesPerPixel

    /**
     * Returns bit shift converting row bytes to row pixels.
     * Returns zero for [ColorType.UNKNOWN].
     *
     * @return  one of: 0, 1, 2, 3, 4; left shift to convert pixels to bytes
     *
     * @see [https://fiddle.skia.org/c/@ImageInfo_shiftPerPixel](https://fiddle.skia.org/c/@ImageInfo_shiftPerPixel)
     */
    val shiftPerPixel: Int
        get() = colorType.shiftPerPixel
    val isGammaCloseToSRGB: Boolean
        get() = colorSpace != null && colorSpace.isGammaCloseToSRGB

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ColorInfo) return false
        if (!other.canEqual(this as Any)) return false
        val `this$_colorType`: Any = colorType
        val `other$_colorType`: Any = other.colorType
        if (`this$_colorType` != `other$_colorType`) return false
        val `this$_alphaType`: Any = alphaType
        val `other$_alphaType`: Any = other.alphaType
        if (`this$_alphaType` != `other$_alphaType`) return false
        val `this$_colorSpace`: Any? = colorSpace
        val `other$_colorSpace`: Any? = other.colorSpace
        return !if (`this$_colorSpace` == null) `other$_colorSpace` != null else `this$_colorSpace` != `other$_colorSpace`
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is ColorInfo
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_colorType`: Any = colorType
        result = result * PRIME + `$_colorType`.hashCode()
        val `$_alphaType`: Any = alphaType
        result = result * PRIME + `$_alphaType`.hashCode()
        val `$_colorSpace`: Any? = colorSpace
        result = result * PRIME + `$_colorSpace`.hashCode()
        return result
    }

    override fun toString(): String {
        return "ColorInfo(_colorType=$colorType, _alphaType=$alphaType, _colorSpace=$colorSpace)"
    }

    fun withColorType(_colorType: ColorType): ColorInfo {
        return if (colorType == _colorType) this else ColorInfo(_colorType, alphaType, colorSpace)
    }

    fun withAlphaType(_alphaType: ColorAlphaType): ColorInfo {
        return if (alphaType == _alphaType) this else ColorInfo(colorType, _alphaType, colorSpace)
    }

    fun withColorSpace(_colorSpace: ColorSpace?): ColorInfo {
        return if (colorSpace === _colorSpace) this else ColorInfo(colorType, alphaType, _colorSpace)
    }

    companion object {
        /**
         * Creates an ColorInfo with [ColorType.UNKNOWN], [ColorAlphaType.UNKNOWN],
         * and no ColorSpace.
         */
        val DEFAULT = ColorInfo(ColorType.UNKNOWN, ColorAlphaType.UNKNOWN, null)
    }
}