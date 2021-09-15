@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

/**
 * Describes how pixel bits encode color. A pixel may be an alpha mask, a
 * grayscale, RGB, or ARGB.
 */
enum class ColorType {
    /**
     * Uninitialized
     */
    UNKNOWN,

    /**
     * Pixel with alpha in 8-bit byte
     */
    ALPHA_8,

    /**
     * Pixel with 5 bits red, 6 bits green, 5 bits blue, in 16-bit word
     */
    RGB_565,

    /**
     * Pixel with 4 bits for alpha, red, green, blue; in 16-bit word
     */
    ARGB_4444,

    /**
     * Pixel with 8 bits for red, green, blue, alpha; in 32-bit word
     */
    RGBA_8888,

    /**
     * Pixel with 8 bits each for red, green, blue; in 32-bit word
     */
    RGB_888X,

    /**
     * Pixel with 8 bits for blue, green, red, alpha; in 32-bit word
     */
    BGRA_8888,

    /**
     * 10 bits for red, green, blue; 2 bits for alpha; in 32-bit word
     */
    RGBA_1010102,

    /**
     * 10 bits for blue, green, red; 2 bits for alpha; in 32-bit word
     */
    BGRA_1010102,

    /**
     * Pixel with 10 bits each for red, green, blue; in 32-bit word
     */
    RGB_101010X,

    /**
     * Pixel with 10 bits each for blue, green, red; in 32-bit word
     */
    BGR_101010X,

    /**
     * Pixel with grayscale level in 8-bit byte
     */
    GRAY_8,

    /**
     * Pixel with half floats in [0,1] for red, green, blue, alpha; in 64-bit word
     */
    RGBA_F16NORM,

    /**
     * Pixel with half floats for red, green, blue, alpha; in 64-bit word
     */
    RGBA_F16,

    /**
     * Pixel using C float for red, green, blue, alpha; in 128-bit word
     */
    RGBA_F32,  // The following 6 colortypes are just for reading from - not for rendering to

    /**
     * Pixel with a uint8_t for red and green
     */
    R8G8_UNORM,

    /**
     * Pixel with a half float for alpha
     */
    A16_FLOAT,

    /**
     * Pixel with a half float for red and green
     */
    R16G16_FLOAT,

    /**
     * Pixel with a little endian uint16_t for alpha
     */
    A16_UNORM,

    /**
     * Pixel with a little endian uint16_t for red and green
     */
    R16G16_UNORM,

    /**
     * Pixel with a little endian uint16_t for red, green, blue, and alpha
     */
    R16G16B16A16_UNORM;

    /**
     * Returns the number of bytes required to store a pixel, including unused padding.
     * Returns zero for [.UNKNOWN].
     *
     * @return  bytes per pixel
     */
    val bytesPerPixel: Int
        get() {
            return when (this) {
                UNKNOWN -> 0
                ALPHA_8 -> 1
                RGB_565 -> 2
                ARGB_4444 -> 2
                RGBA_8888 -> 4
                BGRA_8888 -> 4
                RGB_888X -> 4
                RGBA_1010102 -> 4
                RGB_101010X -> 4
                BGRA_1010102 -> 4
                BGR_101010X -> 4
                GRAY_8 -> 1
                RGBA_F16NORM -> 8
                RGBA_F16 -> 8
                RGBA_F32 -> 16
                R8G8_UNORM -> 2
                A16_UNORM -> 2
                R16G16_UNORM -> 4
                A16_FLOAT -> 2
                R16G16_FLOAT -> 4
                R16G16B16A16_UNORM -> 8
            }
            throw RuntimeException("Unreachable")
        }
    val shiftPerPixel: Int
        get() {
            return when (this) {
                UNKNOWN -> 0
                ALPHA_8 -> 0
                RGB_565 -> 1
                ARGB_4444 -> 1
                RGBA_8888 -> 2
                RGB_888X -> 2
                BGRA_8888 -> 2
                RGBA_1010102 -> 2
                RGB_101010X -> 2
                BGRA_1010102 -> 2
                BGR_101010X -> 2
                GRAY_8 -> 0
                RGBA_F16NORM -> 3
                RGBA_F16 -> 3
                RGBA_F32 -> 4
                R8G8_UNORM -> 1
                A16_UNORM -> 1
                R16G16_UNORM -> 2
                A16_FLOAT -> 1
                R16G16_FLOAT -> 2
                R16G16B16A16_UNORM -> 3
            }
        }


    /**
     *
     * Returns a valid ColorAlphaType for colorType. If there is more than one valid canonical
     * ColorAlphaType, set to alphaType, if valid.
     *
     *
     * Returns null only if alphaType is [ColorAlphaType.UNKNOWN], color type is not
     * [.UNKNOWN], and ColorType is not always opaque.
     *
     * @return  ColorAlphaType if can be associated with colorType
     */
    fun validateAlphaType(alphaType: ColorAlphaType): ColorAlphaType? {
        var alphaType = alphaType
        when (this) {
            UNKNOWN -> alphaType = ColorAlphaType.UNKNOWN
            ALPHA_8, A16_UNORM, A16_FLOAT -> {
                if (ColorAlphaType.UNPREMUL == alphaType) alphaType = ColorAlphaType.PREMUL
                if (ColorAlphaType.UNKNOWN == alphaType) return null
            }
            ARGB_4444, RGBA_8888, BGRA_8888, RGBA_1010102, BGRA_1010102, RGBA_F16NORM, RGBA_F16, RGBA_F32, R16G16B16A16_UNORM -> if (ColorAlphaType.UNKNOWN == alphaType) return null
            GRAY_8, R8G8_UNORM, R16G16_UNORM, R16G16_FLOAT, RGB_565, RGB_888X, RGB_101010X, BGR_101010X -> alphaType =
                ColorAlphaType.OPAQUE
        }
        return alphaType
    }

    fun computeOffset(x: Int, y: Int, rowBytes: Long): Long {
        return if (this == UNKNOWN) 0 else y * rowBytes + (x shl shiftPerPixel)
    }

    fun getR(color: Byte): Float {
        return when (this) {
            GRAY_8 -> (color.toInt() and 0xff) / 255f
            else -> throw IllegalArgumentException("getR(byte) is not supported on ColorType.$this")
        }
    }

    fun getR(color: Short): Float {
        return when (this) {
            RGB_565 -> (color.toInt() shr 11 and 31) / 31f
            ARGB_4444 -> (color.toInt() shr 8 and 0xF) / 15f
            else -> throw IllegalArgumentException("getR(short) is not supported on ColorType.$this")
        }
    }

    fun getR(color: Int): Float {
        return when (this) {
            RGBA_8888 -> (color shr 24 and 0xFF) / 255f
            RGB_888X -> (color shr 24 and 0xFF) / 255f
            BGRA_8888 -> (color shr 8 and 0xFF) / 255f
            RGBA_1010102 -> (color shr 22 and 1023) / 1023f
            RGB_101010X -> (color shr 22 and 1023) / 1023f
            BGRA_1010102 -> (color shr 2 and 1023) / 1023f
            BGR_101010X -> (color shr 2 and 1023) / 1023f
            else -> throw IllegalArgumentException("getR(int) is not supported on ColorType.$this")
        }
    }

    fun getG(color: Byte): Float {
        return when (this) {
            GRAY_8 -> (color.toInt() and 0xff) / 255f
            else -> throw IllegalArgumentException("getG(byte) is not supported on ColorType.$this")
        }
    }

    fun getG(color: Short): Float {
        return when (this) {
            RGB_565 -> (color.toInt() shr 5 and 63) / 63f
            ARGB_4444 -> (color.toInt() shr 4 and 0xF) / 15f
            else -> throw IllegalArgumentException("getG(short) is not supported on ColorType.$this")
        }
    }

    fun getG(color: Int): Float {
        return when (this) {
            RGBA_8888 -> (color shr 16 and 0xFF) / 255f
            RGB_888X -> (color shr 16 and 0xFF) / 255f
            BGRA_8888 -> (color shr 16 and 0xFF) / 255f
            RGBA_1010102 -> (color shr 12 and 1023) / 1023f
            RGB_101010X -> (color shr 12 and 1023) / 1023f
            BGRA_1010102 -> (color shr 12 and 1023) / 1023f
            BGR_101010X -> (color shr 12 and 1023) / 1023f
            else -> throw IllegalArgumentException("getG(int) is not supported on ColorType.$this")
        }
    }

    fun getB(color: Byte): Float {
        return when (this) {
            GRAY_8 -> (color.toInt() and 0xff).toFloat() / 255f
            else -> throw IllegalArgumentException("getB(byte) is not supported on ColorType.$this")
        }
    }

    fun getB(color: Short): Float {
        return when (this) {
            RGB_565 -> (color.toInt() and 31) / 31f
            ARGB_4444 -> (color.toInt() and 0xF) / 15f
            else -> throw IllegalArgumentException("getB(short) is not supported on ColorType.$this")
        }
    }

    fun getB(color: Int): Float {
        return when (this) {
            RGBA_8888 -> (color shr 8 and 0xFF) / 255f
            RGB_888X -> (color shr 8 and 0xFF) / 255f
            BGRA_8888 -> (color shr 24 and 0xFF) / 255f
            RGBA_1010102 -> (color shr 2 and 1023) / 1023f
            RGB_101010X -> (color shr 2 and 1023) / 1023f
            BGRA_1010102 -> (color shr 22 and 1023) / 1023f
            BGR_101010X -> (color shr 22 and 1023) / 1023f
            else -> throw IllegalArgumentException("getB(int) is not supported on ColorType.$this")
        }
    }

    fun getA(color: Byte): Float {
        return when (this) {
            ALPHA_8 -> (color.toInt() and 0xff) / 255f
            else -> throw IllegalArgumentException("getA(byte) is not supported on ColorType.$this")
        }
    }

    fun getA(color: Short): Float {
        return when (this) {
            ARGB_4444 -> (color.toInt() shr 12 and 0xF) / 15f
            else -> throw IllegalArgumentException("getA(short) is not supported on ColorType.$this")
        }
    }

    fun getA(color: Int): Float {
        return when (this) {
            RGBA_8888 -> (color and 0xFF) / 255f
            BGRA_8888 -> (color and 0xFF) / 255f
            RGBA_1010102 -> (color and 3) / 3f
            BGRA_1010102 -> (color and 3) / 3f
            else -> throw IllegalArgumentException("getA(int) is not supported on ColorType.$this")
        }
    }

    companion object {
        /**
         * Native ARGB 32-bit encoding
         */
        var N32 = BGRA_8888
    }

    /**
     * Returns true if ColorType always decodes alpha to 1.0, making the pixel
     * fully opaque. If true, ColorType does not reserve bits to encode alpha.
     *
     * @return  true if alpha is always set to 1.0
     */
    val isAlwaysOpaque: Boolean
        get() {
            return _nIsAlwaysOpaque(ordinal)
        }

}

@ExternalSymbolName("org_jetbrains_skia_ColorType__1nIsAlwaysOpaque")
private external fun _nIsAlwaysOpaque(value: Int): Boolean