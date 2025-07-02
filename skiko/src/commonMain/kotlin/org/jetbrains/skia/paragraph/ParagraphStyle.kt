package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
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
            ParagraphStyle_nEquals(_ptr, getPtr(other))
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
            ParagraphStyle_nGetReplaceTabCharacters(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetReplaceTabCharacters(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var strutStyle: StrutStyle
        get() = try {
            Stats.onNativeCall()
            StrutStyle(ParagraphStyle_nGetStrutStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetStrutStyle(_ptr, getPtr(value))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(value)
        }

    var textStyle: TextStyle
        get() = try {
            Stats.onNativeCall()
            TextStyle(ParagraphStyle_nGetTextStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetTextStyle(_ptr, getPtr(value))
        } finally {
            reachabilityBarrier(value)
            reachabilityBarrier(this)
        }

    var direction: Direction
        get() = try {
            Stats.onNativeCall()
            Direction.values()[ParagraphStyle_nGetDirection(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetDirection(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }


    var alignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment.values()[ParagraphStyle_nGetAlignment(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetAlignment(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    var maxLinesCount: Int
        get() = try {
            Stats.onNativeCall()
            ParagraphStyle_nGetMaxLinesCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetMaxLinesCount(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var ellipsis: String?
        get() = try {
            Stats.onNativeCall()
            val ellipsis = ParagraphStyle_nGetEllipsis(_ptr)
            if (ellipsis == NullPointer) null else withStringResult { ellipsis }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            interopScope {
                ParagraphStyle_nSetEllipsis(_ptr, toInterop(value))
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
            ParagraphStyle_nSetHeight(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }


    var heightMode: HeightMode
        get() = try {
            Stats.onNativeCall()
            HeightMode.values()[ParagraphStyle_nGetHeightMode(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetHeightMode(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    val effectiveAlignment: Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment.values()[ParagraphStyle_nGetEffectiveAlignment(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }

    val isHintingEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            ParagraphStyle_nIsHintingEnabled(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun disableHinting(): ParagraphStyle {
        Stats.onNativeCall()
        ParagraphStyle_nDisableHinting(_ptr)
        return this
    }

    var fontRastrSettings: FontRastrSettings
        get() = try {
            Stats.onNativeCall()
            val edging = FontEdging.values()[ParagraphStyle_nGetEdging(_ptr)]
            Stats.onNativeCall()
            val hinting = FontHinting.values()[ParagraphStyle_nGetHinting(_ptr)]
            Stats.onNativeCall()
            // by some obscure reason kotlinjs makes difference between number encoded booleans returned from `_nGetSubpixel` and regular booleans
            // AssertionError: Expected <FontRastrSettings(edging=ALIAS, hinting=NONE, subpixel=false)>, actual <FontRastrSettings(edging=ALIAS, hinting=NONE, subpixel=0)>
            val subpixel = ParagraphStyle_nGetSubpixel(_ptr).not().not()
            FontRastrSettings(edging, hinting, subpixel)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetFontRastrSettings(_ptr, value.edging.ordinal, value.hinting.ordinal, value.subpixel)
        } finally {
            reachabilityBarrier(this)
        }

    var isApplyRoundingHackEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            ParagraphStyle_nGetApplyRoundingHack(_ptr).not().not()
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetApplyRoundingHack(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var textIndent: TextIndent
        get() = try {
            Stats.onNativeCall()
            val indents = withResult(FloatArray(2)) {
                ParagraphStyle_nGetTextIndent(_ptr, it)
            }
            TextIndent(indents[0], indents[1])
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            ParagraphStyle_nSetTextIndent(_ptr, value.firstLine, value.restLine)
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