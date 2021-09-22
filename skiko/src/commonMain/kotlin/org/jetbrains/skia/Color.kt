package org.jetbrains.skia

object Color {
    // TODO premultiply, alpha
    fun makeLerp(c1: Int, c2: Int, weight: Float): Int {
        val r = (getR(c1) * weight + getR(c2) * (1 - weight)).toInt()
        val g = (getG(c1) * weight + getG(c2) * (1 - weight)).toInt()
        val b = (getB(c1) * weight + getB(c2) * (1 - weight)).toInt()
        return makeRGB(r, g, b)
    }

    fun makeARGB(a: Int, r: Int, g: Int, b: Int): Int {
        require(0 <= a && a <= 255) { "Alpha is out of 0..255 range: $a" }
        require(0 <= r && r <= 255) { "Red is out of 0..255 range: $r" }
        require(0 <= g && g <= 255) { "Green is out of 0..255 range: $g" }
        require(0 <= b && b <= 255) { "Blue is out of 0..255 range: $b" }
        return (a and 0xFF shl 24
                or (r and 0xFF shl 16)
                or (g and 0xFF shl 8)
                or (b and 0xFF))
    }

    fun makeRGB(r: Int, g: Int, b: Int): Int {
        return makeARGB(255, r, g, b)
    }

    fun getA(color: Int): Int {
        return color shr 24 and 0xFF
    }

    fun getR(color: Int): Int {
        return color shr 16 and 0xFF
    }

    fun getG(color: Int): Int {
        return color shr 8 and 0xFF
    }

    fun getB(color: Int): Int {
        return color and 0xFF
    }

    fun withA(color: Int, a: Int): Int {
        require(0 <= a && a <= 255) { "Alpha is out of 0..255 range: $a" }
        return a and 0xFF shl 24 or (color and 0x00FFFFFF)
    }

    fun withR(color: Int, r: Int): Int {
        require(0 <= r && r <= 255) { "Red is out of 0..255 range: $r" }
        return r and 0xFF shl 16 or (color and -0xff0001)
    }

    fun withG(color: Int, g: Int): Int {
        require(0 <= g && g <= 255) { "Green is out of 0..255 range: $g" }
        return g and 0xFF shl 8 or (color and -0xff01)
    }

    fun withB(color: Int, b: Int): Int {
        require(0 <= b && b <= 255) { "Blue is out of 0..255 range: $b" }
        return b and 0xFF or (color and -0x100)
    }

    const val TRANSPARENT: Int   = 0x00_00_00_00
    const val BLACK: Int         = 0xFF_00_00_00.toInt()
    const val WHITE: Int         = 0xFF_FF_FF_FF.toInt()
    const val RED: Int           = 0xFF_FF_00_00.toInt()
    const val GREEN: Int         = 0xFF_00_FF_00.toInt()
    const val BLUE: Int          = 0xFF_00_00_FF.toInt()
    const val YELLOW: Int        = 0xFF_FF_FF_00.toInt()
    const val CYAN: Int          = 0xFF_00_FF_FF.toInt()
    const val MAGENTA: Int       = 0xFF_FF_00_FF.toInt()
}