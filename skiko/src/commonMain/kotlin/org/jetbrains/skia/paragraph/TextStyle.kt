package org.jetbrains.skia.paragraph

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class TextStyle internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(TextStyle_nMake()) {
        Stats.onNativeCall()
    }

    override fun nativeEquals(other: Native?): Boolean {
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
            reachabilityBarrier(this)
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
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
    }

    var decorationStyle: DecorationStyle
        get() = try {
            Stats.onNativeCall()
            DecorationStyle.fromInteropPointer {
                _nGetDecorationStyle(_ptr, it)
            }
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

    val shadows: Array<Shadow>
        get() = try {
            Stats.onNativeCall()
            Shadow.fromInteropPointer(_nGetShadowsCount(_ptr)) {
                _nGetShadows(_ptr, it)
            }
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

            val size = _nGetFontFeaturesSize(_ptr)
            withResult(IntArray(size * 2)) {
                _nGetFontFeatures(_ptr, it)
            }.let {
                FontFeature.fromInteropEncodedBy2Ints(it)
            }
        } finally {
            reachabilityBarrier(this)
        }

    fun addFontFeature(f: FontFeature): TextStyle {
        Stats.onNativeCall()
        interopScope {
            _nAddFontFeature(_ptr, toInterop(f.tag), f.value)
        }
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
            arrayDecoderScope({ ArrayDecoder(TextStyle_nGetFontFamilies(_ptr), ManagedString_nGetFinalizer()) }) { arrayDecoder ->
                (0 until arrayDecoder.size).map { i -> withStringResult(arrayDecoder.release(i)) }
            }.toTypedArray()
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
            val height = TextStyle_nGetHeight(_ptr)
            if (height.isNaN()) null else height
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

    var isHalfLeading: Boolean
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetHalfLeading(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setHalfLeading(value)
        }

    fun setHalfLeading(value: Boolean): TextStyle {
        Stats.onNativeCall()
        TextStyle_nSetHalfLeading(_ptr, value)
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

    var baselineShift: Float
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetBaselineShift(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBaselineShift(value)
        }

    fun setBaselineShift(baselineShift: Float): TextStyle {
        Stats.onNativeCall()
        TextStyle_nSetBaselineShift(_ptr, baselineShift)
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
            reachabilityBarrier(this)
            reachabilityBarrier(typeface)
        }
    }

    var locale: String
        get() = try {
            Stats.onNativeCall()
            withStringResult {
                _nGetLocale(_ptr)
            }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setLocale(value)
        }

    fun setLocale(locale: String?): TextStyle {
        Stats.onNativeCall()
        interopScope {
            _nSetLocale(_ptr, toInterop(locale))
        }
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
            FontMetrics.fromInteropPointer {
                _nGetFontMetrics(_ptr, it)
            }
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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFinalizer")
private external fun TextStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nMake")
private external fun TextStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nEquals")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nEquals")
private external fun TextStyle_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontStyle")
private external fun TextStyle_nGetFontStyle(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetFontStyle")
private external fun TextStyle_nSetFontStyle(ptr: NativePointer, fontStyle: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontSize")
private external fun TextStyle_nGetFontSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetFontSize")
private external fun TextStyle_nSetFontSize(ptr: NativePointer, size: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies")
private external fun TextStyle_nGetFontFamilies(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetHeight")
private external fun TextStyle_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetHeight")
private external fun TextStyle_nSetHeight(ptr: NativePointer, override: Boolean, height: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetHalfLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetHalfLeading")
private external fun TextStyle_nGetHalfLeading(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetHalfLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetHalfLeading")
private external fun TextStyle_nSetHalfLeading(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineShift")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineShift")
private external fun TextStyle_nGetBaselineShift(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineShift")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineShift")
private external fun TextStyle_nSetBaselineShift(ptr: NativePointer, baselineShift: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAttributeEquals")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nAttributeEquals")
private external fun _nAttributeEquals(ptr: NativePointer, attribute: Int, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetColor")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetColor")
private external fun _nGetColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetColor")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetColor")
private external fun _nSetColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetForeground")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetForeground")
private external fun _nGetForeground(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetForeground")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetForeground")
private external fun _nSetForeground(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBackground")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetBackground")
private external fun _nGetBackground(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBackground")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetBackground")
private external fun _nSetBackground(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle")
private external fun _nGetDecorationStyle(ptr: NativePointer, decorationStyle: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetDecorationStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetDecorationStyle")
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

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetShadowsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetShadowsCount")
private external fun _nGetShadowsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetShadows")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetShadows")
private external fun _nGetShadows(ptr: NativePointer, res: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddShadow")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nAddShadow")
private external fun _nAddShadow(ptr: NativePointer, color: Int, offsetX: Float, offsetY: Float, blurSigma: Double)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearShadows")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nClearShadows")
private external fun _nClearShadows(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeatures")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeatures")
private external fun _nGetFontFeatures(ptr: NativePointer, resultIntsArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeaturesSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeaturesSize")
private external fun _nGetFontFeaturesSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature")
private external fun _nAddFontFeature(ptr: NativePointer, name: InteropPointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearFontFeatures")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nClearFontFeatures")
private external fun _nClearFontFeatures(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontFamilies")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetFontFamilies")
private external fun _nSetFontFamilies(ptr: NativePointer, families: InteropPointer, familiesSize: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLetterSpacing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetLetterSpacing")
private external fun _nGetLetterSpacing(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLetterSpacing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetLetterSpacing")
private external fun _nSetLetterSpacing(ptr: NativePointer, letterSpacing: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetWordSpacing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetWordSpacing")
private external fun _nGetWordSpacing(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetWordSpacing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetWordSpacing")
private external fun _nSetWordSpacing(ptr: NativePointer, wordSpacing: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetTypeface")
private external fun _nGetTypeface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetTypeface")
private external fun _nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLocale")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetLocale")
private external fun _nGetLocale(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLocale")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetLocale")
private external fun _nSetLocale(ptr: NativePointer, locale: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineMode")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineMode")
private external fun _nGetBaselineMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineMode")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineMode")
private external fun _nSetBaselineMode(ptr: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontMetrics")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontMetrics")
private external fun _nGetFontMetrics(ptr: NativePointer, fontMetrics: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nIsPlaceholder")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nIsPlaceholder")
private external fun _nIsPlaceholder(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetPlaceholder")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetPlaceholder")
private external fun _nSetPlaceholder(ptr: NativePointer)
