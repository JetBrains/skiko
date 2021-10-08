package org.jetbrains.skia

import kotlin.math.round

class Color4f constructor(val r: Float, val g: Float, val b: Float, val a: Float = 1.0f) {

    constructor(rgba: FloatArray) : this(rgba[0], rgba[1], rgba[2], rgba[3]) {}
    constructor(c: Int) : this(
        (c shr 16 and 255) / 255.0f,
        (c shr 8 and 255) / 255.0f,
        (c and 255) / 255.0f,
        (c shr 24 and 255) / 255.0f
    ) {
    }

    fun toColor(): Int {
        return round(a * 255.0f).toInt() shl 24 or (round(r * 255.0f).toInt() shl 16) or (round(
            g * 255.0f
        ).toInt() shl 8) or round(b * 255.0f).toInt()
    }

    fun flatten(): FloatArray {
        return floatArrayOf(r, g, b, a)
    }

    // TODO premultiply alpha
    fun makeLerp(other: Color4f, weight: Float): Color4f {
        return Color4f(
            r + (other.r - r) * weight,
            g + (other.g - g) * weight,
            b + (other.b - b) * weight,
            a + (other.a - a) * weight
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Color4f) return false
        if (r.compareTo(other.r) != 0) return false
        if (g.compareTo(other.g) != 0) return false
        if (b.compareTo(other.b) != 0) return false
        return a.compareTo(other.a) == 0
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + r.toBits()
        result = result * PRIME + g.toBits()
        result = result * PRIME + b.toBits()
        result = result * PRIME + a.toBits()
        return result
    }

    override fun toString(): String {
        return "Color4f(_r=$r, _g=$g, _b=$b, _a=$a)"
    }

    fun withR(_r: Float): Color4f {
        return if (r == _r) this else Color4f(_r, g, b, a)
    }

    fun withG(_g: Float): Color4f {
        return if (g == _g) this else Color4f(r, _g, b, a)
    }

    fun withB(_b: Float): Color4f {
        return if (b == _b) this else Color4f(r, g, _b, a)
    }

    fun withA(_a: Float): Color4f {
        return if (a == _a) this else Color4f(r, g, b, _a)
    }

    companion object {
        fun flattenArray(colors: Array<Color4f>): FloatArray {
            val arr = FloatArray(colors.size * 4)
            for (i in colors.indices) {
                arr[i * 4] = colors[i].r
                arr[i * 4 + 1] = colors[i].g
                arr[i * 4 + 2] = colors[i].b
                arr[i * 4 + 3] = colors[i].a
            }
            return arr
        }
    }
}