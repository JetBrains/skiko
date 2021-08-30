package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class FontStyleSet @ApiStatus.Internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        fun makeEmpty(): FontStyleSet {
            Stats.onNativeCall()
            return FontStyleSet(_nMakeEmpty())
        }

        external fun _nMakeEmpty(): Long
        external fun _nCount(ptr: Long): Int
        external fun _nGetStyle(ptr: Long, index: Int): Int
        external fun _nGetStyleName(ptr: Long, index: Int): String
        external fun _nGetTypeface(ptr: Long, index: Int): Long
        external fun _nMatchStyle(ptr: Long, style: Int): Long

        init {
            staticLoad()
        }
    }

    fun count(): Int {
        return try {
            Stats.onNativeCall()
            _nCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getStyle(index: Int): FontStyle {
        return try {
            Stats.onNativeCall()
            org.jetbrains.skija.FontStyle(_nGetStyle(_ptr, index))
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getStyleName(index: Int): String {
        return try {
            Stats.onNativeCall()
            _nGetStyleName(_ptr, index)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getTypeface(index: Int): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nGetTypeface(_ptr, index)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun matchStyle(style: FontStyle): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nMatchStyle(_ptr, style._value)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }
}