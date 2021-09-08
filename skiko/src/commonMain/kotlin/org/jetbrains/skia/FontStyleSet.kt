@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import kotlin.jvm.JvmStatic

class FontStyleSet internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makeEmpty(): FontStyleSet {
            Stats.onNativeCall()
            return FontStyleSet(_nMakeEmpty())
        }

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nMakeEmpty")
        external fun _nMakeEmpty(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nCount")
        external fun _nCount(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetStyle")
        external fun _nGetStyle(ptr: NativePointer, index: Int): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetStyleName")
        external fun _nGetStyleName(ptr: NativePointer, index: Int): String
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetTypeface")
        external fun _nGetTypeface(ptr: NativePointer, index: Int): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nMatchStyle")
        external fun _nMatchStyle(ptr: NativePointer, style: Int): NativePointer

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
            if (ptr == NULLPNTR) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun matchStyle(style: FontStyle): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nMatchStyle(_ptr, style._value)
            if (ptr == NULLPNTR) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }
}