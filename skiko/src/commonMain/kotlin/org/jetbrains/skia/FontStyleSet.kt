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


@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nMakeEmpty")
// No ForceNativeThreadState: allocates an empty style set that owns no fonts, so it is short and cannot block.
// Native call path: SkFontStyleSet::CreateEmpty
private external fun FontStyleSet_nMakeEmpty(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nCount")
// Needs ForceNativeThreadState: counts font styles through the DirectWrite font library, whose code cannot be shown to
// avoid blocking or long work.
// Native call path: SkFontStyleSet_DirectWrite::count -> GetFontCount
@ForceNativeThreadState
private external fun _nCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetStyle")
// Needs ForceNativeThreadState: reads the style of a font from a CoreText font descriptor.
// Native call path: SkFontStyleSet_Mac::getStyle -> SkCTFontDescriptorGetSkFontStyle -> CTFontDescriptorCopyAttribute
@ForceNativeThreadState
private external fun _nGetStyle(ptr: NativePointer, index: Int): Int

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetStyleName")
// Needs ForceNativeThreadState: reads a style name from a CoreText font descriptor and converts it to a string.
// Native call path: SkFontStyleSet_Mac::getStyle -> find_desc_str -> CTFontDescriptorCopyAttribute
@ForceNativeThreadState
private external fun _nGetStyleName(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nGetTypeface")
// Needs ForceNativeThreadState: creates a CoreText font from a font descriptor and makes a typeface from it.
// Native call path: SkFontStyleSet_Mac::createTypeface -> create_from_desc -> CTFontCreateWithFontDescriptor
@ForceNativeThreadState
private external fun _nGetTypeface(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontStyleSet__1nMatchStyle")
// Needs ForceNativeThreadState: searches the CoreText font descriptors for the best style match, then creates a
// CoreText font from it.
// Native call path: SkFontStyleSet_Mac::matchStyle -> findMatchingDesc -> create_from_desc ->
//   CTFontCreateWithFontDescriptor
@ForceNativeThreadState
private external fun _nMatchStyle(ptr: NativePointer, style: Int): NativePointer
