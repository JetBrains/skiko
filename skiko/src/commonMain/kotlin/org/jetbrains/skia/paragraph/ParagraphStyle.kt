package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withResult
import org.jetbrains.skia.impl.withStringResult

class ParagraphStyle : Managed(ParagraphStyle_nMake(), _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    override fun nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            _nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    /**
     * Controls whether tab characters are replaced with spaces in text layout.
     *
     * Note:
     * All the platforms except web do not render tab characters even when this is 'false', but put a space instead.
     * With this property we can make web behave similarly, by setting true.
     */
    var replaceTabCharacters: Boolean
        get() = try {
            Stats.onNativeCall()
            _nGetReplaceTabCharacters(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetReplaceTabCharacters(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var strutStyle: StrutStyle
        get() = try {
            Stats.onNativeCall()
            StrutStyle(_nGetStrutStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetStrutStyle(_ptr, getPtr(value))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(value)
        }

    var textStyle: TextStyle
        get() = try {
            Stats.onNativeCall()
            TextStyle(_nGetTextStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetTextStyle(_ptr, getPtr(value))
        } finally {
            reachabilityBarrier(value)
            reachabilityBarrier(this)
        }

    var direction: Direction
        get() = try {
            Stats.onNativeCall()
            Direction.entries[_nGetDirection(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetDirection(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }


    var alignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment.entries[_nGetAlignment(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetAlignment(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    var maxLinesCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetMaxLinesCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetMaxLinesCount(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var ellipsis: String?
        get() = try {
            Stats.onNativeCall()
            val ellipsis = _nGetEllipsis(_ptr)
            if (ellipsis == NullPointer) null else withStringResult { ellipsis }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            interopScope {
                _nSetEllipsis(_ptr, toInterop(value))
            }
        } finally {
            reachabilityBarrier(this)
        }

    var height: Float
        get() = try {
            Stats.onNativeCall()
            ParagraphStyle_nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetHeight(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }


    var heightMode: HeightMode
        get() = try {
            Stats.onNativeCall()
            HeightMode.entries[_nGetHeightMode(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetHeightMode(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    val effectiveAlignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment.entries[_nGetEffectiveAlignment(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }

    val isHintingEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHintingEnabled(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun disableHinting(): ParagraphStyle {
        Stats.onNativeCall()
        _nDisableHinting(_ptr)
        return this
    }

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(
        message = "Replaced by separate properties in TextStyle: fontEdging, fontHinting, subpixel",
        level = DeprecationLevel.ERROR,
    )
    var fontRastrSettings: FontRastrSettings
        get() = textStyle.let {
            FontRastrSettings(it.fontEdging, it.fontHinting, it.subpixel)
        }
        set(value) = textStyle.let {
            it.fontEdging = value.edging
            it.fontHinting = value.hinting
            it.subpixel = value.subpixel
        }

    var isApplyRoundingHackEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            _nGetApplyRoundingHack(_ptr).not().not()
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetApplyRoundingHack(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var textIndent: TextIndent
        get() = try {
            Stats.onNativeCall()
            val indents = withResult(FloatArray(2)) {
                _nGetTextIndent(_ptr, it)
            }
            TextIndent(indents[0], indents[1])
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetTextIndent(_ptr, value.firstLine, value.restLine)
        } finally {
            reachabilityBarrier(this)
        }

    internal object _FinalizerHolder {
        val PTR = ParagraphStyle_nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
    }
}

// Runnable: returns the address of deleteParagraphStyle; the delete itself runs in Managed__invokeFinalizer
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetFinalizer")
private external fun ParagraphStyle_nGetFinalizer(): NativePointer

// Runnable: ParagraphStyle::ParagraphStyle creates a fixed family, null typeface and empty SkPaints
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nMake")
private external fun ParagraphStyle_nMake(): NativePointer

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeight")
private external fun ParagraphStyle_nGetHeight(ptr: NativePointer): Float

// Native: ParagraphStyle::equals -> StrutStyle::operator== and TextStyle::equals scan arbitrary vectors
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nEquals")
@ForceNativeThreadState
private external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

// Native: ParagraphStyle::getStrutStyle -> StrutStyle::operator= copies an arbitrary font-family vector
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetStrutStyle")
@ForceNativeThreadState
private external fun _nGetStrutStyle(ptr: NativePointer): NativePointer

// Native: ParagraphStyle::setStrutStyle -> StrutStyle::StrutStyle copies arbitrary font-family vectors
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetStrutStyle")
@ForceNativeThreadState
private external fun _nSetStrutStyle(ptr: NativePointer, stylePtr: NativePointer)

// Native: ParagraphStyle::getTextStyle -> TextStyle::TextStyle copies arbitrary family, shadow and feature vectors
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextStyle")
@ForceNativeThreadState
private external fun _nGetTextStyle(ptr: NativePointer): NativePointer

// Native: setTextStyle -> TextStyle::operator= -> ~SkTypeface_Mac -> CFRelease of CTFontRef
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextStyle")
@ForceNativeThreadState
private external fun _nSetTextStyle(ptr: NativePointer, textStylePtr: NativePointer)

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetDirection")
private external fun _nGetDirection(ptr: NativePointer): Int

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetDirection")
private external fun _nSetDirection(ptr: NativePointer, direction: Int)

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetAlignment")
private external fun _nGetAlignment(ptr: NativePointer): Int

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetAlignment")
private external fun _nSetAlignment(ptr: NativePointer, align: Int)

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetMaxLinesCount")
private external fun _nGetMaxLinesCount(ptr: NativePointer): Int

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetMaxLinesCount")
private external fun _nSetMaxLinesCount(ptr: NativePointer, maxLines: Int)

// Runnable: copies the ellipsis SkString (refcounted) into a new SkString
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis")
private external fun _nGetEllipsis(ptr: NativePointer): NativePointer

// Native: skString -> SkString::SkString -> SkString::Rec::Make scans and copies arbitrary ellipsis bytes
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis")
@ForceNativeThreadState
private external fun _nSetEllipsis(ptr: NativePointer, ellipsis: InteropPointer)

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeight")
private external fun _nSetHeight(ptr: NativePointer, height: Float)

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeightMode")
private external fun _nGetHeightMode(ptr: NativePointer): Int

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeightMode")
private external fun _nSetHeightMode(ptr: NativePointer, v: Int)

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEffectiveAlignment")
private external fun _nGetEffectiveAlignment(ptr: NativePointer): Int

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nIsHintingEnabled")
private external fun _nIsHintingEnabled(ptr: NativePointer): Boolean

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nDisableHinting")
private external fun _nDisableHinting(ptr: NativePointer)

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetApplyRoundingHack")
private external fun _nGetApplyRoundingHack(ptr: NativePointer): Boolean

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetApplyRoundingHack")
private external fun _nSetApplyRoundingHack(ptr: NativePointer, value: Boolean)

// Runnable: fills a local TextIndent of two floats and copies it into the style
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextIndent")
private external fun _nSetTextIndent(ptr: NativePointer, firstLine: Float, restLine: Float)

// Runnable: copies a TextIndent of two floats into the result buffer
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextIndent")
private external fun _nGetTextIndent(ptr: NativePointer, result: InteropPointer)

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetReplaceTabCharacters")
private external fun _nGetReplaceTabCharacters(ptr: NativePointer): Boolean

// Runnable: ParagraphStyle accessor reads or writes a plain scalar or enum field
@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetReplaceTabCharacters")
private external fun _nSetReplaceTabCharacters(ptr: NativePointer, value: Boolean)
