package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.*

class TextStyle internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(TextStyle_nMake()) {
        Stats.onNativeCall()
    }

    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            TextStyle_nEquals(
                _ptr,
                getPtr(other)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    fun equals(attribute: TextStyleAttribute, other: TextStyle?): Boolean {
        return try {
            Stats.onNativeCall()
            _nAttributeEquals(
                _ptr,
                attribute.ordinal,
                getPtr(other)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    var color: Int
        get() = try {
            Stats.onNativeCall()
            _nGetColor(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColor(value)
        }
    
    fun setColor(color: Int): TextStyle {
        Stats.onNativeCall()
        _nSetColor(_ptr, color)
        return this
    }

    var foreground: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetForeground(_ptr)
            if (ptr == NullPointer) null else Paint(ptr, true)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setForeground(value)
        }
    
    fun setForeground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetForeground(
                _ptr,
                getPtr(paint)
            )
            this
        } finally {
            reachabilityBarrier(paint)
        }
    }

    var background: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetBackground(_ptr)
            if (ptr == NullPointer) null else Paint(ptr, true)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBackground(value)
        }

    fun setBackground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetBackground(
                _ptr,
                getPtr(paint)
            )
            this
        } finally {
            reachabilityBarrier(paint)
        }
    }

    var decorationStyle: org.jetbrains.skia.paragraph.DecorationStyle
        get() = try {
            Stats.onNativeCall()
            _nGetDecorationStyle(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setDecorationStyle(value)
        }
    
    fun setDecorationStyle(d: DecorationStyle): TextStyle {
        Stats.onNativeCall()
        _nSetDecorationStyle(
            _ptr,
            d._underline,
            d._overline,
            d._lineThrough,
            d._gaps,
            d.color,
            d._lineStyle.ordinal,
            d.thicknessMultiplier
        )
        return this
    }

    var fontStyle: FontStyle
        get() = try {
            Stats.onNativeCall()
            FontStyle(TextStyle_nGetFontStyle(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontStyle(value)
        }
    
    fun setFontStyle(s: FontStyle): TextStyle {
        Stats.onNativeCall()
        TextStyle_nSetFontStyle(_ptr, s._value)
        return this
    }

    val shadows: Array<org.jetbrains.skia.paragraph.Shadow>
        get() = try {
            Stats.onNativeCall()
            _nGetShadows(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun addShadow(s: Shadow): TextStyle {
        Stats.onNativeCall()
        _nAddShadow(_ptr, s.color, s.offsetX, s.offsetY, s.blurSigma)
        return this
    }

    fun addShadows(shadows: Array<Shadow>): TextStyle {
        for (s in shadows) addShadow(s)
        return this
    }

    fun clearShadows(): TextStyle {
        Stats.onNativeCall()
        _nClearShadows(_ptr)
        return this
    }

    val fontFeatures: Array<FontFeature>
        get() = try {
            Stats.onNativeCall()
            _nGetFontFeatures(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun addFontFeature(f: FontFeature): TextStyle {
        Stats.onNativeCall()
        _nAddFontFeature(_ptr, f.tag, f.value)
        return this
    }

    fun addFontFeatures(FontFeatures: Array<FontFeature>): TextStyle {
        for (s in FontFeatures) addFontFeature(s)
        return this
    }

    fun clearFontFeatures(): TextStyle {
        Stats.onNativeCall()
        _nClearFontFeatures(_ptr)
        return this
    }

    var fontSize: Float
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetFontSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontSize(value)
        }
    
    fun setFontSize(size: Float): TextStyle {
        Stats.onNativeCall()
        TextStyle_nSetFontSize(_ptr, size)
        return this
    }

    var fontFamilies: Array<String>
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetFontFamilies(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setFontFamilies(value)
        }

    fun setFontFamily(family: String): TextStyle {
        return setFontFamilies(arrayOf(family))
    }

    fun setFontFamilies(families: Array<String>?): TextStyle {
        Stats.onNativeCall()
        interopScope {
            _nSetFontFamilies(_ptr, toInterop(families), families?.size ?: 0)
        }
        return this
    }

    var height: Float?
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHeight(value)
        }
    
    fun setHeight(height: Float?): TextStyle {
        Stats.onNativeCall()
        if (height == null) TextStyle_nSetHeight(_ptr, false, 0f) else TextStyle_nSetHeight(_ptr, true, height)
        return this
    }

    var letterSpacing: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLetterSpacing(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setLetterSpacing(value)
        }

    fun setLetterSpacing(letterSpacing: Float): TextStyle {
        Stats.onNativeCall()
        _nSetLetterSpacing(_ptr, letterSpacing)
        return this
    }

    var wordSpacing: Float
        get() = try {
            Stats.onNativeCall()
            _nGetWordSpacing(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setWordSpacing(value)
        }
    
    fun setWordSpacing(wordSpacing: Float): TextStyle {
        Stats.onNativeCall()
        _nSetWordSpacing(_ptr, wordSpacing)
        return this
    }

    var typeface: Typeface?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetTypeface(_ptr)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setTypeface(value)
        }
    
    fun setTypeface(typeface: Typeface?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetTypeface(
                _ptr,
                getPtr(typeface)
            )
            this
        } finally {
            reachabilityBarrier(typeface)
        }
    }

    var locale: String
        get() = try {
            Stats.onNativeCall()
            _nGetLocale(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setLocale(value)
        }

    fun setLocale(locale: String?): TextStyle {
        Stats.onNativeCall()
        _nSetLocale(_ptr, locale)
        return this
    }

    var baselineMode: BaselineMode
        get() = try {
            Stats.onNativeCall()
            BaselineMode.values().get(_nGetBaselineMode(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBaselineMode(value)
        }
    
    fun setBaselineMode(baseline: BaselineMode): TextStyle {
        Stats.onNativeCall()
        _nSetBaselineMode(_ptr, baseline.ordinal)
        return this
    }

    val fontMetrics: FontMetrics
        get() = try {
            Stats.onNativeCall()
            _nGetFontMetrics(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    val isPlaceholder: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsPlaceholder(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun setPlaceholder(): TextStyle {
        Stats.onNativeCall()
        _nSetPlaceholder(_ptr)
        return this
    }

    internal object _FinalizerHolder {
        val PTR = TextStyle_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFinalizer")
private external fun TextStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nMake")
private external fun TextStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nEquals")
private external fun TextStyle_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontStyle")
private external fun TextStyle_nGetFontStyle(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontStyle")
private external fun TextStyle_nSetFontStyle(ptr: NativePointer, fontStyle: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontSize")
private external fun TextStyle_nGetFontSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontSize")
private external fun TextStyle_nSetFontSize(ptr: NativePointer, size: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies")
private external fun TextStyle_nGetFontFamilies(ptr: NativePointer): Array<String>

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetHeight")
private external fun TextStyle_nGetHeight(ptr: NativePointer): Float?

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetHeight")
private external fun TextStyle_nSetHeight(ptr: NativePointer, override: Boolean, height: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAttributeEquals")
private external fun _nAttributeEquals(ptr: NativePointer, attribute: Int, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetColor")
private external fun _nGetColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetColor")
private external fun _nSetColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetForeground")
private external fun _nGetForeground(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetForeground")
private external fun _nSetForeground(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBackground")
private external fun _nGetBackground(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBackground")
private external fun _nSetBackground(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle")
private external fun _nGetDecorationStyle(ptr: NativePointer): DecorationStyle

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetDecorationStyle")
private external fun _nSetDecorationStyle(
    ptr: NativePointer,
    underline: Boolean,
    overline: Boolean,
    lineThrough: Boolean,
    gaps: Boolean,
    color: Int,
    style: Int,
    thicknessMultiplier: Float
)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetShadows")
private external fun _nGetShadows(ptr: NativePointer): Array<Shadow>

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddShadow")
private external fun _nAddShadow(ptr: NativePointer, color: Int, offsetX: Float, offsetY: Float, blurSigma: Double)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearShadows")
private external fun _nClearShadows(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeatures")
private external fun _nGetFontFeatures(ptr: NativePointer): Array<FontFeature>

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature")
private external fun _nAddFontFeature(ptr: NativePointer, name: String?, value: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearFontFeatures")
private external fun _nClearFontFeatures(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontFamilies")
private external fun _nSetFontFamilies(ptr: NativePointer, families: InteropPointer, familiesSize: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLetterSpacing")
private external fun _nGetLetterSpacing(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLetterSpacing")
private external fun _nSetLetterSpacing(ptr: NativePointer, letterSpacing: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetWordSpacing")
private external fun _nGetWordSpacing(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetWordSpacing")
private external fun _nSetWordSpacing(ptr: NativePointer, wordSpacing: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetTypeface")
private external fun _nGetTypeface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetTypeface")
private external fun _nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLocale")
private external fun _nGetLocale(ptr: NativePointer): String

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLocale")
private external fun _nSetLocale(ptr: NativePointer, locale: String?)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineMode")
private external fun _nGetBaselineMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineMode")
private external fun _nSetBaselineMode(ptr: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontMetrics")
private external fun _nGetFontMetrics(ptr: NativePointer): FontMetrics

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nIsPlaceholder")
private external fun _nIsPlaceholder(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetPlaceholder")
private external fun _nSetPlaceholder(ptr: NativePointer)
