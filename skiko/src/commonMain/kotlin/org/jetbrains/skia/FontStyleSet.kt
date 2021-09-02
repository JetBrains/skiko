package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class FontStyleSet internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        fun makeEmpty(): FontStyleSet {
            Stats.onNativeCall()
            return FontStyleSet(_nMakeEmpty())
        }

        @JvmStatic external fun _nMakeEmpty(): Long
        @JvmStatic external fun _nCount(ptr: Long): Int
        @JvmStatic external fun _nGetStyle(ptr: Long, index: Int): Int
        @JvmStatic external fun _nGetStyleName(ptr: Long, index: Int): String
        @JvmStatic external fun _nGetTypeface(ptr: Long, index: Int): Long
        @JvmStatic external fun _nMatchStyle(ptr: Long, style: Int): Long

        init {
            staticLoad()
        }
    }

    fun count(): Int {
        return try {
            Stats.onNativeCall()
            _nCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getStyle(index: Int): FontStyle {
        return try {
            Stats.onNativeCall()
            FontStyle(_nGetStyle(_ptr, index))
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getStyleName(index: Int): String {
        return try {
            Stats.onNativeCall()
            _nGetStyleName(_ptr, index)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getTypeface(index: Int): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nGetTypeface(_ptr, index)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun matchStyle(style: FontStyle): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nMatchStyle(_ptr, style._value)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }
}