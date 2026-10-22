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

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of the delete function without calling it, so it is short and cannot
// block.
// Native call path: deleteParagraphStyle
private external fun ParagraphStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nMake")
// No ForceNativeThreadState: creates a style with a fixed font family, no typeface and empty paints, so it is short and
// cannot block.
// Native call path: ParagraphStyle::ParagraphStyle
private external fun ParagraphStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeight")
// No ForceNativeThreadState: reads a plain number or enum field from the style, so it is short and cannot block.
// Native call path: ParagraphStyle::getHeight
private external fun ParagraphStyle_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nEquals")
// Needs ForceNativeThreadState: compares the strut style's font family list and the text styles, so its time grows with
// how many entries they hold.
// Native call path: ParagraphStyle::equals -> almostEquals -> StrutStyle::operator==
@ForceNativeThreadState
private external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetStrutStyle")
// Needs ForceNativeThreadState: copies the strut style, including its font family list, so its time grows with the
// number of families.
// Native call path: ParagraphStyle::getStrutStyle -> StrutStyle::operator=
@ForceNativeThreadState
private external fun _nGetStrutStyle(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetStrutStyle")
// Needs ForceNativeThreadState: copies the strut style argument with its font families, then destroys the old strings,
// so its time grows with the family count.
// Native call path: ParagraphStyle::setStrutStyle -> StrutStyle::StrutStyle
@ForceNativeThreadState
private external fun _nSetStrutStyle(ptr: NativePointer, stylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextStyle")
// Needs ForceNativeThreadState: copies the text style, including its font family, shadow and feature lists, so its time
// grows with their lengths.
// Native call path: ParagraphStyle::getTextStyle -> TextStyle::TextStyle
@ForceNativeThreadState
private external fun _nGetTextStyle(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextStyle")
// Needs ForceNativeThreadState: replacing the default text style can drop the last reference to the old typeface, which
// releases a CoreText font.
// Native call path: ParagraphStyle::setTextStyle -> TextStyle::operator= -> ~SkTypeface_Mac -> CFRelease
@ForceNativeThreadState
private external fun _nSetTextStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetDirection")
// No ForceNativeThreadState: reads a plain enum field from the style, so it is short and cannot block.
// Native call path: ParagraphStyle::getDirection
private external fun _nGetDirection(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetDirection")
// No ForceNativeThreadState: stores a plain enum field in the style, so it is short and cannot block.
// Native call path: ParagraphStyle::setDirection
private external fun _nSetDirection(ptr: NativePointer, direction: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetAlignment")
// No ForceNativeThreadState: reads a plain enum field from the style, so it is short and cannot block.
// Native call path: ParagraphStyle::getAlignment
private external fun _nGetAlignment(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetAlignment")
// No ForceNativeThreadState: stores a plain enum field in the style, so it is short and cannot block.
// Native call path: ParagraphStyle::setAlignment
private external fun _nSetAlignment(ptr: NativePointer, align: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetMaxLinesCount")
// No ForceNativeThreadState: reads a plain number field from the style, so it is short and cannot block.
// Native call path: ParagraphStyle::getMaxLines
private external fun _nGetMaxLinesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetMaxLinesCount")
// No ForceNativeThreadState: stores a plain number field in the style, so it is short and cannot block.
// Native call path: ParagraphStyle::setMaxLines
private external fun _nSetMaxLinesCount(ptr: NativePointer, maxLines: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis")
// No ForceNativeThreadState: copies the ellipsis string, which is reference counted, into a new string, so it is short
// and cannot block.
// Native call path: ParagraphStyle::getEllipsis
private external fun _nGetEllipsis(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis")
// Needs ForceNativeThreadState: scans and copies the ellipsis string, so its time grows with the length of the
// ellipsis.
// Native call path: skString -> SkString::SkString -> SkString::Rec::Make
@ForceNativeThreadState
private external fun _nSetEllipsis(ptr: NativePointer, ellipsis: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeight")
// No ForceNativeThreadState: stores a plain number field in the style, so it is short and cannot block.
// Native call path: ParagraphStyle::setHeight
private external fun _nSetHeight(ptr: NativePointer, height: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeightMode")
// No ForceNativeThreadState: reads a plain enum field from the style, so it is short and cannot block.
// Native call path: ParagraphStyle::getHeightMode
private external fun _nGetHeightMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeightMode")
// No ForceNativeThreadState: stores a plain enum field in the style, so it is short and cannot block.
// Native call path: ParagraphStyle::setHeightMode
private external fun _nSetHeightMode(ptr: NativePointer, v: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEffectiveAlignment")
// No ForceNativeThreadState: picks an alignment value from a few fields, so it is short and cannot block.
// Native call path: ParagraphStyle::getEffectiveAlignment
private external fun _nGetEffectiveAlignment(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nIsHintingEnabled")
// No ForceNativeThreadState: reads a stored on or off flag from the style, so it is short and cannot block.
// Native call path: ParagraphStyle::isHintingEnabled
private external fun _nIsHintingEnabled(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nDisableHinting")
// No ForceNativeThreadState: stores an on or off flag in the style, so it is short and cannot block.
// Native call path: ParagraphStyle::disableHinting
private external fun _nDisableHinting(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetApplyRoundingHack")
// No ForceNativeThreadState: reads a stored on or off flag from the style, so it is short and cannot block.
// Native call path: ParagraphStyle::getApplyRoundingHack
private external fun _nGetApplyRoundingHack(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetApplyRoundingHack")
// No ForceNativeThreadState: stores an on or off flag in the style, so it is short and cannot block.
// Native call path: ParagraphStyle::setApplyRoundingHack
private external fun _nSetApplyRoundingHack(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextIndent")
// No ForceNativeThreadState: fills a small local indent of two numbers and stores it in the style, so it is short and
// cannot block.
// Native call path: TextIndent::setFirstLine -> TextIndent::setRestLine -> ParagraphStyle::setTextIndent
private external fun _nSetTextIndent(ptr: NativePointer, firstLine: Float, restLine: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextIndent")
// No ForceNativeThreadState: copies an indent of two numbers into the result buffer, so it is short and cannot block.
// Native call path: ParagraphStyle::getTextIndent
private external fun _nGetTextIndent(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetReplaceTabCharacters")
// No ForceNativeThreadState: reads a stored on or off flag from the style, so it is short and cannot block.
// Native call path: ParagraphStyle::getReplaceTabCharacters
private external fun _nGetReplaceTabCharacters(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetReplaceTabCharacters")
// No ForceNativeThreadState: stores an on or off flag in the style, so it is short and cannot block.
// Native call path: ParagraphStyle::setReplaceTabCharacters
private external fun _nSetReplaceTabCharacters(ptr: NativePointer, value: Boolean)
