package org.jetbrains.skia.impl

import org.jetbrains.skia.CubicResampler
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.IPoint
import org.jetbrains.skia.IRange
import java.lang.ref.Reference

actual abstract class Native actual constructor(ptr: Long) {
    actual var _ptr: Long

    actual companion object {
        actual val NULLPNTR: NativePointer
            get() = 0L
    }

    override fun toString(): String {
        return javaClass.simpleName + "(_ptr=0x" + _ptr.toString(16) + ")"
    }

    override fun equals(other: Any?): Boolean {
        return try {
            if (this === other) return true
            if (null == other) return false
            if (!javaClass.isInstance(other)) return false
            val nOther = other as Native
            if (_ptr == nOther._ptr) true else _nativeEquals(nOther)
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    // FIXME two different pointers might point to equal objects
    override fun hashCode(): Int {
        return java.lang.Long.hashCode(_ptr)
    }

    actual open fun _nativeEquals(other: Native?): Boolean {
        return false
    }

    init {
        if (ptr == NULLPNTR) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual fun reachabilityBarrier(obj: Any?) {
    Reference.reachabilityFence(obj)
}

actual typealias NativePointer = Long

actual fun toIRange(p: NativePointer): IRange = IRange((p ushr 32).toInt(), (p and -1).toInt())

actual fun toIPoint(p: NativePointer): IPoint = IPoint((p ushr 32).toInt(), (p and -1).toInt())

actual fun Int.toNativePointer(): NativePointer = toLong()
actual fun CubicResampler._actualPack(): NativePointer {
    return ((b.toBits().toULong() shl 32) or c.toBits().toULong()).toLong()
}

actual fun FilterMipmap._actualPack(): NativePointer {
    return filterMode.ordinal.toLong() shl 32 or mipmapMode.ordinal.toLong()
}