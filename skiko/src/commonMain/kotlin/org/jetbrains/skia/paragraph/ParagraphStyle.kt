package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
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
            Direction.values()[_nGetDirection(_ptr)]
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
            Alignment.values()[_nGetAlignment(_ptr)]
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
            HeightMode.values()[_nGetHeightMode(_ptr)]
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
            Alignment.values()[_nGetEffectiveAlignment(_ptr)]
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

    var fontRastrSettings: FontRastrSettings
        get() = try {
            Stats.onNativeCall()
            val edging = FontEdging.values()[_nGetEdging(_ptr)]
            Stats.onNativeCall()
            val hinting = FontHinting.values()[_nGetHinting(_ptr)]
            Stats.onNativeCall()
            // by some obscure reason kotlinjs makes difference between number encoded booleans returned from `_nGetSubpixel` and regular booleans
            // AssertionError: Expected <FontRastrSettings(edging=ALIAS, hinting=NONE, subpixel=false)>, actual <FontRastrSettings(edging=ALIAS, hinting=NONE, subpixel=0)>
            val subpixel = _nGetSubpixel(_ptr).not().not()
            FontRastrSettings(edging, hinting, subpixel)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetFontRastrSettings(_ptr, value.edging.ordinal, value.hinting.ordinal, value.subpixel)
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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetFinalizer")
private external fun ParagraphStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nMake")
private external fun ParagraphStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeight")
private external fun ParagraphStyle_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nEquals")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nEquals")
private external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetStrutStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetStrutStyle")
private external fun _nGetStrutStyle(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetStrutStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetStrutStyle")
private external fun _nSetStrutStyle(ptr: NativePointer, stylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextStyle")
private external fun _nGetTextStyle(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextStyle")
private external fun _nSetTextStyle(ptr: NativePointer, textStylePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetDirection")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetDirection")
private external fun _nGetDirection(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetDirection")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetDirection")
private external fun _nSetDirection(ptr: NativePointer, direction: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetAlignment")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetAlignment")
private external fun _nGetAlignment(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetAlignment")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetAlignment")
private external fun _nSetAlignment(ptr: NativePointer, align: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetMaxLinesCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetMaxLinesCount")
private external fun _nGetMaxLinesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetMaxLinesCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetMaxLinesCount")
private external fun _nSetMaxLinesCount(ptr: NativePointer, maxLines: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEllipsis")
private external fun _nGetEllipsis(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetEllipsis")
private external fun _nSetEllipsis(ptr: NativePointer, ellipsis: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeight")
private external fun _nSetHeight(ptr: NativePointer, height: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeightMode")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHeightMode")
private external fun _nGetHeightMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeightMode")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetHeightMode")
private external fun _nSetHeightMode(ptr: NativePointer, v: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEffectiveAlignment")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEffectiveAlignment")
private external fun _nGetEffectiveAlignment(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nIsHintingEnabled")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nIsHintingEnabled")
private external fun _nIsHintingEnabled(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nDisableHinting")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nDisableHinting")
private external fun _nDisableHinting(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetFontRastrSettings")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetFontRastrSettings")
private external fun _nSetFontRastrSettings(ptr: NativePointer, edging: Int, hinting: Int, subpixel: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEdging")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetEdging")
private external fun _nGetEdging(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHinting")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetHinting")
private external fun _nGetHinting(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetSubpixel")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetSubpixel")
private external fun _nGetSubpixel(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextIndent")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nSetTextIndent")
private external fun _nSetTextIndent(ptr: NativePointer, firstLine: Float, restLine: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextIndent")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_ParagraphStyle__1nGetTextIndent")
private external fun _nGetTextIndent(ptr: NativePointer, result: InteropPointer)