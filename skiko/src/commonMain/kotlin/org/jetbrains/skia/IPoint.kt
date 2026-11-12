package org.jetbrains.skia

import org.jetbrains.skiko.internal.packInts
import org.jetbrains.skiko.internal.unpackInt1
import org.jetbrains.skiko.internal.unpackInt2
import kotlin.jvm.JvmInline

inline fun IPoint(x: Int, y: Int): IPoint = IPoint(packInts(x, y))
@JvmInline
value class IPoint(val packedValue: Long) {

    inline val x: Int
        get() = unpackInt1(packedValue)

    inline val y: Int
        get() = unpackInt2(packedValue)

    fun offset(dx: Int, dy: Int): IPoint {
        return IPoint(x + dx, y + dy)
    }

    fun offset(vec: IPoint): IPoint {
        return offset(vec.x, vec.y)
    }

    val isEmpty: Boolean
        get() = x <= 0 || y <= 0

    override fun toString(): String {
        return "IPoint(x=$x, y=$y)"
    }

    companion object {
        val ZERO: Point
            get() = Point(0x0L)
    }
}
