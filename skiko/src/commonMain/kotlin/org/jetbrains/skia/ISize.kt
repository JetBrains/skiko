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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ISize) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (width != other.width) return false
        return if (height != other.height) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is ISize
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + width
        result = result * PRIME + height
        return result
    }

    override fun toString(): String {
        return "ISize(_width=" + width + ", _height=" + height + ")"
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