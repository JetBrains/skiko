package org.jetbrains.skija

/**
 *
 * Describes pixel and encoding. ImageInfo can be created from ColorInfo by
 * providing dimensions.
 *
 *
 * It encodes how pixel bits describe alpha, transparency; color components red, blue,
 * and green; and ColorSpace, the range and linearity of colors.
 */
class ColorInfo(colorType: ColorType, alphaType: ColorAlphaType, colorSpace: ColorSpace?) {
    val colorType: ColorType
    val alphaType: ColorAlphaType
    val colorSpace: ColorSpace?
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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ColorInfo) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_colorType`: Any = colorType
        val `other$_colorType`: Any = other.colorType
        if (if (`this$_colorType` == null) `other$_colorType` != null else `this$_colorType` != `other$_colorType`) return false
        val `this$_alphaType`: Any = alphaType
        val `other$_alphaType`: Any = other.alphaType
        if (if (`this$_alphaType` == null) `other$_alphaType` != null else `this$_alphaType` != `other$_alphaType`) return false
        val `this$_colorSpace`: Any? = colorSpace
        val `other$_colorSpace`: Any? = other.colorSpace
        return if (if (`this$_colorSpace` == null) `other$_colorSpace` != null else `this$_colorSpace` != `other$_colorSpace`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is ColorInfo
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_colorType`: Any = colorType
        result = result * PRIME + (`$_colorType`?.hashCode() ?: 43)
        val `$_alphaType`: Any = alphaType
        result = result * PRIME + (`$_alphaType`?.hashCode() ?: 43)
        val `$_colorSpace`: Any? = colorSpace
        result = result * PRIME + (`$_colorSpace`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "ColorInfo(_colorType=" + colorType + ", _alphaType=" + alphaType + ", _colorSpace=" + colorSpace + ")"
    }

    fun withColorType(_colorType: ColorType): ColorInfo {
        if (_colorType == null) {
            throw NullPointerException("_colorType is marked non-null but is null")
        }
        return if (colorType == _colorType) this else ColorInfo(_colorType, alphaType, colorSpace)
    }

    fun withAlphaType(_alphaType: ColorAlphaType): ColorInfo {
        if (_alphaType == null) {
            throw NullPointerException("_alphaType is marked non-null but is null")
        }
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

    init {
        this.colorType = colorType
        this.alphaType = alphaType
        this.colorSpace = colorSpace
    }
}