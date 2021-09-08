package org.jetbrains.skia

import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer

/**
 *
 * Describes pixel dimensions and encoding. Bitmap, Image, Pixmap, and Surface
 * can be created from ImageInfo. ImageInfo can be retrieved from Bitmap and
 * Pixmap, but not from Image and Surface. For example, Image and Surface
 * implementations may defer pixel depth, so may not completely specify ImageInfo.
 *
 *
 * ImageInfo contains dimensions, the pixel integral width and height. It encodes
 * how pixel bits describe alpha, transparency; color components red, blue,
 * and green; and ColorSpace, the range and linearity of colors.
 */
class ImageInfo(val colorInfo: ColorInfo, val width: Int, val height: Int) {

    constructor(width: Int, height: Int, colorType: ColorType, alphaType: ColorAlphaType) : this(
        ColorInfo(
            colorType,
            alphaType,
            null
        ), width, height
    ) {
    }

    constructor(
        width: Int,
        height: Int,
        colorType: ColorType,
        alphaType: ColorAlphaType,
        colorSpace: ColorSpace?
    ) : this(ColorInfo(colorType, alphaType, colorSpace), width, height) {
    }

    internal constructor(width: Int, height: Int, colorType: Int, alphaType: Int, colorSpace: NativePointer) : this(
        width,
        height,
        ColorType.values()[colorType],
        ColorAlphaType.values()[alphaType],
        if (colorSpace == Native.NullPointer) null else ColorSpace(colorSpace)
    )

    /**
     * Returns minimum bytes per row, computed from pixel getWidth() and ColorType, which
     * specifies getBytesPerPixel(). Bitmap maximum value for row bytes must fit
     * in 31 bits.
     */
    val minRowBytes: Long
        get() = (width * bytesPerPixel).toLong()

    val colorType: ColorType
        get() = colorInfo.colorType

    fun withColorType(colorType: ColorType): ImageInfo {
        return withColorInfo(colorInfo.withColorType(colorType))
    }

    val colorAlphaType: ColorAlphaType
        get() = colorInfo.alphaType

    fun withColorAlphaType(alphaType: ColorAlphaType): ImageInfo {
        return withColorInfo(colorInfo.withAlphaType(alphaType))
    }

    val colorSpace: ColorSpace?
        get() = colorInfo.colorSpace

    fun withColorSpace(colorSpace: ColorSpace): ImageInfo {
        return withColorInfo(colorInfo.withColorSpace(colorSpace))
    }

    /**
     * @return  true if either dimension is zero or smaller
     */
    val isEmpty: Boolean
        get() = width <= 0 || height <= 0

    /**
     *
     * Returns true if ColorAlphaType is set to hint that all pixels are opaque; their
     * alpha value is implicitly or explicitly 1.0. If true, and all pixels are
     * not opaque, Skia may draw incorrectly.
     *
     *
     * Does not check if ColorType allows alpha, or if any pixel value has
     * transparency.
     *
     * @return  true if alphaType is [ColorAlphaType.OPAQUE]
     */
    val isOpaque: Boolean
        get() = colorInfo.isOpaque

    /**
     * @return  integral rectangle from (0, 0) to (getWidth(), getHeight())
     */
    val bounds: IRect
        get() = IRect.makeXYWH(0, 0, width, height)

    /**
     * @return  true if associated ColorSpace is not null, and ColorSpace gamma
     * is approximately the same as sRGB.
     */
    val isGammaCloseToSRGB: Boolean
        get() = colorInfo.isGammaCloseToSRGB

    fun withWidthHeight(width: Int, height: Int): ImageInfo {
        return ImageInfo(colorInfo, width, height)
    }

    /**
     * Returns number of bytes per pixel required by ColorType.
     * Returns zero if [.getColorType] is [ColorType.UNKNOWN].
     *
     * @return  bytes in pixel
     */
    val bytesPerPixel: Int
        get() = colorInfo.bytesPerPixel

    /**
     * Returns bit shift converting row bytes to row pixels.
     * Returns zero for [ColorType.UNKNOWN].
     *
     * @return  one of: 0, 1, 2, 3, 4; left shift to convert pixels to bytes
     */
    val shiftPerPixel: Int
        get() = colorInfo.shiftPerPixel

    /**
     * Returns true if rowBytes is valid for this ImageInfo.
     *
     * @param rowBytes  size of pixel row including padding
     * @return          true if rowBytes is large enough to contain pixel row and is properly aligned
     */
    fun isRowBytesValid(rowBytes: Long): Boolean {
        if (rowBytes < minRowBytes) return false
        val shift = shiftPerPixel
        return rowBytes shr shift shl shift == rowBytes
    }

    /**
     *
     * Returns byte offset of pixel from pixel base address.
     *
     *
     * Asserts in debug build if x or y is outside of bounds. Does not assert if
     * rowBytes is smaller than [.getMinRowBytes], even though result may be incorrect.
     *
     * @param x         column index, zero or greater, and less than getWidth()
     * @param y         row index, zero or greater, and less than getHeight()
     * @param rowBytes  size of pixel row or larger
     * @return          offset within pixel array
     *
     * @see [https://fiddle.skia.org/c/@ImageInfo_computeOffset](https://fiddle.skia.org/c/@ImageInfo_computeOffset)
     */
    fun computeOffset(x: Int, y: Int, rowBytes: Long): Long {
        return colorInfo.colorType.computeOffset(x, y, rowBytes)
    }

    /**
     *
     * Returns storage required by pixel array, given ImageInfo dimensions, ColorType,
     * and rowBytes. rowBytes is assumed to be at least as large as [.getMinRowBytes].
     *
     *
     * Returns zero if height is zero.
     *
     * @param rowBytes  size of pixel row or larger
     * @return          memory required by pixel buffer
     *
     * @see [https://fiddle.skia.org/c/@ImageInfo_computeByteSize](https://fiddle.skia.org/c/@ImageInfo_computeByteSize)
     */
    fun computeByteSize(rowBytes: Long): Long {
        return if (0 == height) 0 else (height - 1) * rowBytes + width * bytesPerPixel
    }

    /**
     *
     * Returns storage required by pixel array, given ImageInfo dimensions, and
     * ColorType. Uses [.getMinRowBytes] to compute bytes for pixel row.
     *
     * Returns zero if height is zero.
     *
     * @return  least memory required by pixel buffer
     */
    fun computeMinByteSize(): Long {
        return computeByteSize(minRowBytes)
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ImageInfo) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        val `this$_colorInfo`: Any? = colorInfo
        val `other$_colorInfo`: Any? = other.colorInfo
        return if (if (`this$_colorInfo` == null) `other$_colorInfo` != null else `this$_colorInfo` != `other$_colorInfo`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is ImageInfo
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + width
        result = result * PRIME + height
        val `$_colorInfo`: Any? = colorInfo
        result = result * PRIME + (`$_colorInfo`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "ImageInfo(_colorInfo=" + colorInfo + ", _width=" + width + ", _height=" + height + ")"
    }

    fun withColorInfo(_colorInfo: ColorInfo): ImageInfo {
        return if (colorInfo === _colorInfo) this else ImageInfo(_colorInfo, width, height)
    }

    fun withWidth(_width: Int): ImageInfo {
        return if (width == _width) this else ImageInfo(colorInfo, _width, height)
    }

    fun withHeight(_height: Int): ImageInfo {
        return if (height == _height) this else ImageInfo(colorInfo, width, _height)
    }

    companion object {
        val DEFAULT = ImageInfo(ColorInfo.DEFAULT, 0, 0)

        /**
         * @return  ImageInfo with [ColorType.N32]
         */
        fun makeN32(width: Int, height: Int, alphaType: ColorAlphaType): ImageInfo {
            return ImageInfo(ColorInfo(ColorType.N32, alphaType, null), width, height)
        }

        /**
         * @return  ImageInfo with [ColorType.N32]
         */
        fun makeN32(width: Int, height: Int, alphaType: ColorAlphaType, colorSpace: ColorSpace?): ImageInfo {
            return ImageInfo(ColorInfo(ColorType.N32, alphaType, colorSpace), width, height)
        }

        /**
         * @return  ImageInfo with [ColorType.N32] and [ColorSpace.getSRGB]
         *
         * @see [https://fiddle.skia.org/c/@ImageInfo_MakeS32](https://fiddle.skia.org/c/@ImageInfo_MakeS32)
         */
        fun makeS32(width: Int, height: Int, alphaType: ColorAlphaType): ImageInfo {
            return ImageInfo(
                ColorInfo(ColorType.N32, alphaType, ColorSpace.sRGB),
                width,
                height
            )
        }

        /**
         * @return  ImageInfo with [ColorType.N32] and [ColorAlphaType.PREMUL]
         */
        fun makeN32Premul(width: Int, height: Int): ImageInfo {
            return ImageInfo(ColorInfo(ColorType.N32, ColorAlphaType.PREMUL, null), width, height)
        }

        /**
         * @return  ImageInfo with [ColorType.N32] and [ColorAlphaType.PREMUL]
         */
        fun makeN32Premul(width: Int, height: Int, colorSpace: ColorSpace?): ImageInfo {
            return ImageInfo(ColorInfo(ColorType.N32, ColorAlphaType.PREMUL, colorSpace), width, height)
        }

        /**
         * @return  ImageInfo with [ColorType.ALPHA_8] and [ColorAlphaType.PREMUL]
         */
        fun makeA8(width: Int, height: Int): ImageInfo {
            return ImageInfo(ColorInfo(ColorType.ALPHA_8, ColorAlphaType.PREMUL, null), width, height)
        }

        /**
         * @return  ImageInfo with [ColorType.UNKNOWN] and [ColorAlphaType.UNKNOWN]
         */
        fun makeUnknown(width: Int, height: Int): ImageInfo {
            return ImageInfo(ColorInfo(ColorType.UNKNOWN, ColorAlphaType.UNKNOWN, null), width, height)
        }
    }
}
