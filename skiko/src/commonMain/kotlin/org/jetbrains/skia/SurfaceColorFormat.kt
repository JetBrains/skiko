package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class SurfaceColorFormat internal constructor(val ordinal: Int) {
    companion object {
        val UNKNOWN = SurfaceColorFormat(0)  //!< uninitialized
        val ALPHA_8 = SurfaceColorFormat(1)  //!< pixel with alpha in 8-bit byte
        val RGB_565 = SurfaceColorFormat(2)  //!< pixel with 5 bits red, 6 bits green, 5 bits blue, in 16-bit word
        val ARGB_4444 = SurfaceColorFormat(3)  //!< pixel with 4 bits for alpha, red, green, blue; in 16-bit word
        val RGBA_8888 = SurfaceColorFormat(4)  //!< pixel with 8 bits for red, green, blue, alpha; in 32-bit word
        val RGB_888x = SurfaceColorFormat(5)  //!< pixel with 8 bits each for red, green, blue; in 32-bit word
        val BGRA_8888 = SurfaceColorFormat(6)  //!< pixel with 8 bits for blue, green, red, alpha; in 32-bit word
        val RGBA_1010102 = SurfaceColorFormat(7)  //!< 10 bits for red, green, blue; 2 bits for alpha; in 32-bit word
        val RGB_101010x = SurfaceColorFormat(8)  //!< pixel with 10 bits each for red, green, blue; in 32-bit word
        val GRAY_8 = SurfaceColorFormat(9)  //!< pixel with grayscale level in 8-bit byte
        val RGBA_F16_NORM =
            SurfaceColorFormat(10)  //!< pixel with half floats in [0,1] for red, green, blue, alpha; in 64-bit word
        val RGBA_F16 = SurfaceColorFormat(11)  //!< pixel with half floats for red, green, blue, alpha; in 64-bit word
        val RGBA_F32 = SurfaceColorFormat(12)  //!< pixel using C float for red, green, blue, alpha; in 128-bit word

        // The following 6 colortypes are just for reading from - not for rendering to
        val R8G8_UNORM = SurfaceColorFormat(13)  //<! pixel with a uint8_t for red and green
        val A16_FLOAT = SurfaceColorFormat(14)  //<! pixel with a half float for alpha
        val R16G16_FLOAT = SurfaceColorFormat(15)  //<! pixel with a half float for red and green
        val A16_UNORM = SurfaceColorFormat(16)  //<! pixel with a little endian uint16_t for alpha
        val R16G16_UNORM = SurfaceColorFormat(17)  //<! pixel with a little endian uint16_t for red and green
        val R16G16B16A16_UNORM = SurfaceColorFormat(18)
    }
}