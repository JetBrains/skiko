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
            withStringResult {
                _nGetStyleName(_ptr, index)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun getTypeface(index: Int): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nGetTypeface(_ptr, index)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun matchStyle(style: FontStyle): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nMatchStyle(_ptr, style._value)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }
}


// Runnable: SkFontStyleSet::CreateEmpty allocates SkEmptyFontStyleSet, which owns nothing
@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nMakeEmpty")
private external fun FontStyleSet_nMakeEmpty(): NativePointer

// Native: SkFontStyleSet_DirectWrite::count -> GetFontCount lacks a source proof of bounded work
@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nCount")
@ForceNativeThreadState
private external fun _nCount(ptr: NativePointer): Int

// Native: SkFontStyleSet_Mac::getStyle -> SkCTFontDescriptorGetSkFontStyle -> CTFontDescriptorCopyAttribute
@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetStyle")
@ForceNativeThreadState
private external fun _nGetStyle(ptr: NativePointer, index: Int): Int

// Native: SkFontStyleSet_Mac::getStyle -> find_desc_str -> CTFontDescriptorCopyAttribute, SkStringFromCFString
@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetStyleName")
@ForceNativeThreadState
private external fun _nGetStyleName(ptr: NativePointer, index: Int): NativePointer

// Native: SkFontStyleSet_Mac::createTypeface -> create_from_desc -> CTFontCreateWithFontDescriptor
@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetTypeface")
@ForceNativeThreadState
private external fun _nGetTypeface(ptr: NativePointer, index: Int): NativePointer

// Native: SkFontStyleSet_Mac::matchStyle -> findMatchingDesc -> create_from_desc -> CTFontCreateWithFontDescriptor
@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nMatchStyle")
@ForceNativeThreadState
private external fun _nMatchStyle(ptr: NativePointer, style: Int): NativePointer
