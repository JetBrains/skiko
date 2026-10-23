package org.jetbrains.skia

import org.jetbrains.skiko.internal.packInts
import org.jetbrains.skiko.internal.unpackInt1
import org.jetbrains.skiko.internal.unpackInt2
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

inline fun ISize(width: Int, height: Int): ISize = ISize(packInts(width, height))
@JvmInline
value class ISize(val packedValue: Long) {

    inline val width: Int
        get() = unpackInt1(packedValue)

    inline val height: Int
        get() = unpackInt2(packedValue)

    fun isZero(): Boolean = packedValue == 0x0L

    fun isEmpty(): Boolean = (width <= 0) || (height <= 0)

    fun area(): Int = width * height

    override fun toString(): String {
        return "ISize(width=$width, height=$height)"
    }

    companion object {
        val ZERO: ISize
            get() = ISize(0x0L)

        @JvmStatic
        fun make(w: Int, h: Int): ISize {
            return ISize(w, h)
        }

        @JvmStatic
        fun makeEmpty(): ISize = ZERO
    }
}