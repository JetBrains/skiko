package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.nativeCall
import org.jetbrains.skia.impl.nativeCallFromStatic
import org.jetbrains.skia.impl.withStringResult
import kotlin.jvm.JvmStatic

class FontStyleSet internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        @JvmStatic
        fun makeEmpty(): FontStyleSet = nativeCallFromStatic { FontStyleSet(FontStyleSet_nMakeEmpty()) }

        init {
            staticLoad()
        }
    }

    fun count(): Int = nativeCall { _nCount(_ptr) }

    fun getStyle(index: Int): FontStyle = nativeCall { FontStyle(_nGetStyle(_ptr, index)) }

    fun getStyleName(index: Int): String = nativeCall { withStringResult { _nGetStyleName(_ptr, index) } }

    fun getTypeface(index: Int): Typeface? = nativeCall {
        val ptr = _nGetTypeface(_ptr, index)
        if (ptr == NullPointer) null else Typeface(ptr)
    }

    fun matchStyle(style: FontStyle): Typeface? = nativeCall {
        val ptr = _nMatchStyle(_ptr, style._value)
        if (ptr == NullPointer) null else Typeface(ptr)
    }
}

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
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
// </editor-fold>