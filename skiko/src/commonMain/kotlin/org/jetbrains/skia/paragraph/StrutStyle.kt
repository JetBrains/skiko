package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.nativeCall
import org.jetbrains.skia.impl.pointerOrNullPointer
import org.jetbrains.skia.impl.withResult
import org.jetbrains.skia.impl.withStringResult

class StrutStyle internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(StrutStyle_nMake()) {
        Stats.onNativeCall()
    }

    override fun nativeEquals(other: Native?): Boolean =
        nativeCall(other) { other -> StrutStyle_nEquals(_ptr, other.pointerOrNullPointer) }

    val fontFamilies: Array<String>
        get() = nativeCall {
            arrayDecoderScope({ArrayDecoder(_nGetFontFamilies(_ptr), ManagedString_nGetFinalizer())}) { arrayDecoder ->
                (0 until arrayDecoder.size).map {  i -> withStringResult(arrayDecoder.release(i)) }.toTypedArray()
            }
        }

    fun setFontFamilies(families: Array<String>): StrutStyle = nativeCall {
        interopScope {
            StrutStyle_nSetFontFamilies(_ptr, toInterop(families), families.size)
        }
        this
    }


    var fontStyle: FontStyle
        get() = nativeCall {
            val fontStyleData = withResult(IntArray(3)) {
                _nGetFontStyle(_ptr, it)
            }
            FontStyle(FontWeight(fontStyleData[0]), FontWidth(fontStyleData[1]), FontSlant.entries[fontStyleData[2]])
        }
        set(value) {
            setFontStyle(value)
        }
    
    fun setFontStyle(style: FontStyle): StrutStyle = nativeCall {
        _nSetFontStyle(_ptr, style._value)
        this
    }

    var fontSize: Float
        get() = nativeCall { _nGetFontSize(_ptr) }
        set(value) {
            setFontSize(value)
        }

    fun setFontSize(value: Float): StrutStyle = nativeCall {
        check(!value.isNaN())
        _nSetFontSize(_ptr, value)
        this
    }

    var height: Float
        get() = nativeCall { StrutStyle_nGetHeight(_ptr) }
        set(value) {
            setHeight(value)
        }
    
    fun setHeight(value: Float): StrutStyle = nativeCall {
        check(!value.isNaN())
        StrutStyle_nSetHeight(_ptr, value)
        this
    }

    var leading: Float
        get() = nativeCall { _nGetLeading(_ptr) }
        set(value) {
            setLeading(value)
        }
    
    fun setLeading(value: Float): StrutStyle = nativeCall {
        check(!value.isNaN())
        _nSetLeading(_ptr, value)
        this
    }

    var isEnabled: Boolean
        get() = nativeCall { _nIsEnabled(_ptr) }
        set(value) {
            setEnabled(value)
        }
    
    fun setEnabled(value: Boolean): StrutStyle = nativeCall {
        StrutStyle_nSetEnabled(_ptr, value)
        this
    }

    var isHeightForced: Boolean
        get() = nativeCall { _nIsHeightForced(_ptr) }
        set(value) {
            setHeightForced(value)
        }
    
    fun setHeightForced(value: Boolean): StrutStyle = nativeCall {
        _nSetHeightForced(_ptr, value)
        this
    }

    var isHeightOverridden: Boolean
        get() = nativeCall { _nIsHeightOverridden(_ptr) }
        set(value) {
            setHeightOverridden(value)
        }
    
    fun setHeightOverridden(value: Boolean): StrutStyle = nativeCall {
        _nSetHeightOverridden(_ptr, value)
        this
    }

    @Deprecated("Replaced by topRatio")
    var isHalfLeading: Boolean
        get() = nativeCall { _nIsHalfLeading(_ptr) }
        set(value) {
            @Suppress("DEPRECATION")
            setHalfLeading(value)
        }

    // Same as topRatio = halfLeading ? 0.5f : -1.0f
    @Deprecated("Replaced by topRatio")
    fun setHalfLeading(value: Boolean): StrutStyle = nativeCall {
        _nSetHalfLeading(_ptr, value)
        this
    }

    // [0..1]: the ratio of ascent to ascent+descent
    // -1: proportional to the ascent/descent
    var topRatio: Float
        get() = nativeCall { StrutStyle_nGetTopRatio(_ptr) }
        set(value) {
            setTopRatio(value)
        }

    fun setTopRatio(topRatio: Float): StrutStyle = nativeCall {
        StrutStyle_nSetTopRatio(_ptr, topRatio)
        this
    }

    private object _FinalizerHolder {
        val PTR = StrutStyle_nGetFinalizer()
    }
}

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of the delete function without calling it, so it is short and cannot
// block.
// Native call path: deleteStrutStyle
private external fun StrutStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nMake")
// No ForceNativeThreadState: creates a strut style and sets its number fields, with no typeface or Skia object, so it
// is short and cannot block.
// Native call path: StrutStyle::StrutStyle
private external fun StrutStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nEquals")
// Needs ForceNativeThreadState: compares font family lists and name text, so its time grows with the number and length
// of family names.
// Native call path: StrutStyle::operator== -> SkString::equals
@ForceNativeThreadState
private external fun StrutStyle_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetHeight")
// No ForceNativeThreadState: reads a stored number or flag from the strut style, so it is short and cannot block.
// Native call path: StrutStyle::getHeight
private external fun StrutStyle_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHeight")
// No ForceNativeThreadState: stores a number or flag in the strut style, so it is short and cannot block.
// Native call path: StrutStyle::setHeight
private external fun StrutStyle_nSetHeight(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetEnabled")
// No ForceNativeThreadState: stores an on or off flag in the strut style, so it is short and cannot block.
// Native call path: StrutStyle::setEnabled
private external fun StrutStyle_nSetEnabled(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies")
// Needs ForceNativeThreadState: builds a new string object for every font family, so its time grows with the number of
// families.
// Native call path: StrutStyle::getFontFamilies
@ForceNativeThreadState
private external fun _nGetFontFamilies(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies")
// Needs ForceNativeThreadState: copies every font family name and destroys the old list, so its time grows with the
// number and length of names.
// Native call path: skStringVector -> skString -> SkString::SkString
@ForceNativeThreadState
private external fun StrutStyle_nSetFontFamilies(ptr: NativePointer, families: InteropPointer, familiesCount: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle")
// No ForceNativeThreadState: reads a stored font style value from the strut style, so it is short and cannot block.
// Native call path: StrutStyle::getFontStyle
private external fun _nGetFontStyle(ptr: NativePointer, fontStyleData: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle")
// No ForceNativeThreadState: stores a font style value in the strut style, so it is short and cannot block.
// Native call path: StrutStyle::setFontStyle
private external fun _nSetFontStyle(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontSize")
// No ForceNativeThreadState: reads a stored font size from the strut style, so it is short and cannot block.
// Native call path: StrutStyle::getFontSize
private external fun _nGetFontSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontSize")
// No ForceNativeThreadState: stores a font size in the strut style, so it is short and cannot block.
// Native call path: StrutStyle::setFontSize
private external fun _nSetFontSize(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetLeading")
// No ForceNativeThreadState: reads a stored leading value from the strut style, so it is short and cannot block.
// Native call path: StrutStyle::getLeading
private external fun _nGetLeading(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetLeading")
// No ForceNativeThreadState: stores a leading value in the strut style, so it is short and cannot block.
// Native call path: StrutStyle::setLeading
private external fun _nSetLeading(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsEnabled")
// No ForceNativeThreadState: reads a stored on or off flag from the strut style, so it is short and cannot block.
// Native call path: StrutStyle::isEnabled
private external fun _nIsEnabled(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightForced")
// No ForceNativeThreadState: reads a stored on or off flag from the strut style, so it is short and cannot block.
// Native call path: StrutStyle::isHeightForced
private external fun _nIsHeightForced(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightForced")
// No ForceNativeThreadState: stores an on or off flag in the strut style, so it is short and cannot block.
// Native call path: StrutStyle::setHeightForced
private external fun _nSetHeightForced(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightOverridden")
// No ForceNativeThreadState: reads a stored on or off flag from the strut style, so it is short and cannot block.
// Native call path: StrutStyle::isHeightOverridden
private external fun _nIsHeightOverridden(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightOverridden")
// No ForceNativeThreadState: stores an on or off flag in the strut style, so it is short and cannot block.
// Native call path: StrutStyle::setHeightOverridden
private external fun _nSetHeightOverridden(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsHalfLeading")
// No ForceNativeThreadState: reads a stored on or off flag from the strut style, so it is short and cannot block.
// Native call path: StrutStyle::isHalfLeading
private external fun _nIsHalfLeading(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHalfLeading")
// No ForceNativeThreadState: stores an on or off flag in the strut style, so it is short and cannot block.
// Native call path: StrutStyle::setHalfLeading
private external fun _nSetHalfLeading(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetTopRatio")
// No ForceNativeThreadState: reads a stored top ratio from the strut style, so it is short and cannot block.
// Native call path: StrutStyle::getTopRatio
private external fun StrutStyle_nGetTopRatio(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetTopRatio")
// No ForceNativeThreadState: stores a top ratio in the strut style, so it is short and cannot block.
// Native call path: StrutStyle::setTopRatio
private external fun StrutStyle_nSetTopRatio(ptr: NativePointer, value: Float)
// </editor-fold>