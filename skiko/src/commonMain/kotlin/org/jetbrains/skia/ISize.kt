package org.jetbrains.skia

import kotlin.jvm.JvmStatic

class ISize internal constructor(val width: Int, val height: Int) {
    fun isZero(): Boolean {
        return (width == 0) && (height == 0)
    }

    fun isEmpty(): Boolean {
        return (width <= 0) || (height <= 0)
    }

    fun area(): Int {
        return width * height
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ISize) return false
        if (width != other.width) return false
        return height == other.height
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + width
        result = result * PRIME + height
        return result
    }

    override fun toString(): String {
        return "ISize(_width=$width, _height=$height)"
    }

    companion object {
        @JvmStatic
        fun make(w: Int, h: Int): ISize {
            return ISize(w, h)
        }

        @JvmStatic
        fun makeEmpty(): ISize {
            return ISize(0, 0)
        }
    }
}