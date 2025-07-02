package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.withStringResult

class FontStyleSet internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makeEmpty(): FontStyleSet {
            Stats.onNativeCall()
            return FontStyleSet(FontStyleSet_nMakeEmpty())
        }

        init {
            staticLoad()
        }
    }

    fun count(): Int {
        return try {
            Stats.onNativeCall()
            FontStyleSet_nCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getStyle(index: Int): FontStyle {
        return try {
            Stats.onNativeCall()
            FontStyle(FontStyleSet_nGetStyle(_ptr, index))
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getStyleName(index: Int): String {
        return try {
            Stats.onNativeCall()
            withStringResult {
                FontStyleSet_nGetStyleName(_ptr, index)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getTypeface(index: Int): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = FontStyleSet_nGetTypeface(_ptr, index)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun matchStyle(style: FontStyle): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = FontStyleSet_nMatchStyle(_ptr, style._value)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }
}