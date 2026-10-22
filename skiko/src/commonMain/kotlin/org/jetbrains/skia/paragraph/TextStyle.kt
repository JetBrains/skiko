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
        try {
            Stats.onNativeCall()
            _nSetColor(_ptr, color)
        } finally {
            reachabilityBarrier(this)
        }
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
        try {
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
        } finally {
            reachabilityBarrier(this)
        }
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
        try {
            Stats.onNativeCall()
            TextStyle_nSetFontStyle(_ptr, s._value)
        } finally {
            reachabilityBarrier(this)
        }
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
        try {
            Stats.onNativeCall()
            _nAddShadow(_ptr, s.color, s.offsetX, s.offsetY, s.blurSigma)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    fun addShadows(shadows: Array<Shadow>): TextStyle {
        for (s in shadows) addShadow(s)
        return this
    }

    fun clearShadows(): TextStyle {
        try {
            Stats.onNativeCall()
            _nClearShadows(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    val fontFeatures: Array<FontFeature>
        get() = try {
            Stats.onNativeCall()
            val size = _nGetFontFeaturesSize(_ptr)
            withResult(IntArray(size * 2)) {
                Stats.onNativeCall()
                _nGetFontFeatures(_ptr, it)
            }.let {
                FontFeature.fromInteropEncodedBy2Ints(it)
            }
        } finally {
            reachabilityBarrier(this)
        }

    fun addFontFeature(f: FontFeature): TextStyle {
        try {
            Stats.onNativeCall()
            interopScope {
                _nAddFontFeature(_ptr, toInterop(f.tag), f.value)
            }
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    fun addFontFeatures(FontFeatures: Array<FontFeature>): TextStyle {
        for (s in FontFeatures) addFontFeature(s)
        return this
    }

    fun clearFontFeatures(): TextStyle {
        try {
            Stats.onNativeCall()
            _nClearFontFeatures(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
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
        check(!size.isNaN())
        try {
            Stats.onNativeCall()
            TextStyle_nSetFontSize(_ptr, size)
        } finally {
            reachabilityBarrier(this)
        }
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
        if (height == null) {
            try {
                Stats.onNativeCall()
                TextStyle_nSetHeight(_ptr, false, 0f)
            } finally {
                reachabilityBarrier(this)
            }
        } else {
            check(!height.isNaN())
            try {
                Stats.onNativeCall()
                TextStyle_nSetHeight(_ptr, true, height)
            } finally {
                reachabilityBarrier(this)
            }
        }
        return this
    }

    @Deprecated("Replaced by topRatio")
    var isHalfLeading: Boolean
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetHalfLeading(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            @Suppress("DEPRECATION")
            setHalfLeading(value)
        }

    // Same as topRatio = halfLeading ? 0.5f : -1.0f
    @Deprecated("Replaced by topRatio")
    fun setHalfLeading(value: Boolean): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nSetHalfLeading(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    // [0..1]: the ratio of ascent to ascent+descent
    // -1: proportional to the ascent/descent
    var topRatio: Float
        get() = try {
            Stats.onNativeCall()
            TextStyle_nGetTopRatio(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setTopRatio(value)
        }

    fun setTopRatio(topRatio: Float): TextStyle {
        try {
            Stats.onNativeCall()
            TextStyle_nSetTopRatio(_ptr, topRatio)
        } finally {
            reachabilityBarrier(this)
        }
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
        check(!letterSpacing.isNaN())
        try {
            Stats.onNativeCall()
            _nSetLetterSpacing(_ptr, letterSpacing)
        } finally {
            reachabilityBarrier(this)
        }
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
        check(!baselineShift.isNaN())
        try {
            Stats.onNativeCall()
            TextStyle_nSetBaselineShift(_ptr, baselineShift)
        } finally {
            reachabilityBarrier(this)
        }
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
        check(!wordSpacing.isNaN())
        try {
            Stats.onNativeCall()
            _nSetWordSpacing(_ptr, wordSpacing)
        } finally {
            reachabilityBarrier(this)
        }
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
        try {
            Stats.onNativeCall()
            interopScope {
                _nSetLocale(_ptr, toInterop(locale))
            }
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var baselineMode: BaselineMode
        get() = try {
            Stats.onNativeCall()
            BaselineMode.entries[_nGetBaselineMode(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBaselineMode(value)
        }

    fun setBaselineMode(baseline: BaselineMode): TextStyle {
        try {
            Stats.onNativeCall()
            _nSetBaselineMode(_ptr, baseline.ordinal)
        } finally {
            reachabilityBarrier(this)
        }
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
        try {
            Stats.onNativeCall()
            _nSetPlaceholder(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    var fontEdging: FontEdging
        get() = try {
            Stats.onNativeCall()
            FontEdging.entries[_nGetFontEdging(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetFontEdging(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    var subpixel: Boolean
        get() = try {
            Stats.onNativeCall()
            _nGetSubpixel(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetSubpixel(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var fontHinting: FontHinting
        get() = try {
            Stats.onNativeCall()
            FontHinting.entries[_nGetFontHinting(_ptr)]
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetFontHinting(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    internal object _FinalizerHolder {
        val PTR = TextStyle_nGetFinalizer()
    }
}

// Runnable: returns the address of deleteTextStyle; the finalizer itself runs via Managed__invokeFinalizer
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFinalizer")
private external fun TextStyle_nGetFinalizer(): NativePointer

// Runnable: TextStyle() copies one fixed default family and creates paints with null effect refs and no typeface
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nMake")
private external fun TextStyle_nMake(): NativePointer

// Native: TextStyle::equals compares arbitrary-length family, shadow and feature vectors
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nEquals")
@ForceNativeThreadState
private external fun TextStyle_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

// Runnable: TextStyle::getFontStyle returns a stored SkFontStyle packed into an int
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontStyle")
private external fun TextStyle_nGetFontStyle(ptr: NativePointer): Int

// Runnable: TextStyle::setFontStyle stores a packed SkFontStyle value
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontStyle")
private external fun TextStyle_nSetFontStyle(ptr: NativePointer, fontStyle: Int)

// Runnable: TextStyle::getFontSize returns a stored field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontSize")
private external fun TextStyle_nGetFontSize(ptr: NativePointer): Float

// Runnable: TextStyle::setFontSize stores a field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontSize")
private external fun TextStyle_nSetFontSize(ptr: NativePointer, size: Float)

// Native: TextStyle::getFontFamilies feeds an arbitrary-length loop allocating SkString wrappers
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies")
@ForceNativeThreadState
private external fun TextStyle_nGetFontFamilies(ptr: NativePointer): NativePointer

// Runnable: TextStyle::getHeightOverride/getHeight return stored fields
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetHeight")
private external fun TextStyle_nGetHeight(ptr: NativePointer): Float

// Runnable: TextStyle::setHeightOverride/setHeight store fields
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetHeight")
private external fun TextStyle_nSetHeight(ptr: NativePointer, override: Boolean, height: Float)

// Runnable: TextStyle::getHalfLeading compares a stored field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetHalfLeading")
private external fun TextStyle_nGetHalfLeading(ptr: NativePointer): Boolean

// Runnable: TextStyle::setHalfLeading stores a field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetHalfLeading")
private external fun TextStyle_nSetHalfLeading(ptr: NativePointer, value: Boolean)

// Runnable: TextStyle::getTopRatio returns a stored field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetTopRatio")
private external fun TextStyle_nGetTopRatio(ptr: NativePointer): Float

// Runnable: TextStyle::setTopRatio stores a field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetTopRatio")
private external fun TextStyle_nSetTopRatio(ptr: NativePointer, value: Float)

// Runnable: TextStyle::getBaselineShift returns a stored field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineShift")
private external fun TextStyle_nGetBaselineShift(ptr: NativePointer): Float

// Runnable: TextStyle::setBaselineShift stores a field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineShift")
private external fun TextStyle_nSetBaselineShift(ptr: NativePointer, baselineShift: Float)

// Native: TextStyle::equalsByFonts and TextStyle::matchOneAttribute compare arbitrary-length vectors
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAttributeEquals")
@ForceNativeThreadState
private external fun _nAttributeEquals(ptr: NativePointer, attribute: Int, otherPtr: NativePointer): Boolean

// Runnable: TextStyle::getColor returns a stored SkColor
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetColor")
private external fun _nGetColor(ptr: NativePointer): Int

// Runnable: TextStyle::setColor stores a SkColor
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetColor")
private external fun _nSetColor(ptr: NativePointer, color: Int)

// Runnable: new SkPaint copy only adds refs; TextStyle keeps its own refs so no last unref
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetForeground")
private external fun _nGetForeground(ptr: NativePointer): NativePointer

// Native: setForegroundColor -> assign fForeground drops old SkPaint -> last unref of shader or GPU image
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetForeground")
@ForceNativeThreadState
private external fun _nSetForeground(ptr: NativePointer, paintPtr: NativePointer)

// Runnable: new SkPaint copy only adds refs; TextStyle keeps its own refs so no last unref
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBackground")
private external fun _nGetBackground(ptr: NativePointer): NativePointer

// Native: setBackgroundColor -> assign fBackground drops old SkPaint -> last unref of shader or GPU image
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBackground")
@ForceNativeThreadState
private external fun _nSetBackground(ptr: NativePointer, paintPtr: NativePointer)

// Runnable: TextStyle::getDecoration returns a small POD copied into the result array
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle")
private external fun _nGetDecorationStyle(ptr: NativePointer, decorationStyle: InteropPointer)

// Runnable: TextStyle decoration setters store POD fields
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

// Native: TextStyle::getShadows copies the entire shadow vector before size()
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetShadowsCount")
@ForceNativeThreadState
private external fun _nGetShadowsCount(ptr: NativePointer): Int

// Native: TextStyle::getShadows copies every shadow, then the binding serializes the whole vector
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetShadows")
@ForceNativeThreadState
private external fun _nGetShadows(ptr: NativePointer, res: InteropPointer)

// Native: TextStyle::addShadow -> std::vector::emplace_back may relocate the entire shadow vector
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddShadow")
@ForceNativeThreadState
private external fun _nAddShadow(ptr: NativePointer, color: Int, offsetX: Float, offsetY: Float, blurSigma: Double)

// Native: TextStyle::resetShadows -> std::vector::clear may traverse every shadow
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearShadows")
@ForceNativeThreadState
private external fun _nClearShadows(ptr: NativePointer)

// Native: TextStyle::getFontFeatures copies all features; skija::FontFeature::writeToIntArray loops over them
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeatures")
@ForceNativeThreadState
private external fun _nGetFontFeatures(ptr: NativePointer, resultIntsArray: InteropPointer)

// Native: TextStyle::getFontFeatures copies the entire feature vector before size()
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeaturesSize")
@ForceNativeThreadState
private external fun _nGetFontFeaturesSize(ptr: NativePointer): Int

// Native: TextStyle::addFontFeature -> std::vector::emplace_back may relocate every existing feature
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature")
@ForceNativeThreadState
private external fun _nAddFontFeature(ptr: NativePointer, name: InteropPointer, value: Int)

// Native: TextStyle::resetFontFeatures -> std::vector::clear destroys every FontFeature and SkString
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearFontFeatures")
@ForceNativeThreadState
private external fun _nClearFontFeatures(ptr: NativePointer)

// Native: skStringVector loops over all families; skString -> SkString::SkString scans and copies names
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontFamilies")
@ForceNativeThreadState
private external fun _nSetFontFamilies(ptr: NativePointer, families: InteropPointer, familiesSize: Int)

// Runnable: TextStyle::getLetterSpacing returns a stored field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLetterSpacing")
private external fun _nGetLetterSpacing(ptr: NativePointer): Float

// Runnable: TextStyle::setLetterSpacing stores a field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLetterSpacing")
private external fun _nSetLetterSpacing(ptr: NativePointer, letterSpacing: Float)

// Runnable: TextStyle::getWordSpacing returns a stored field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetWordSpacing")
private external fun _nGetWordSpacing(ptr: NativePointer): Float

// Runnable: TextStyle::setWordSpacing stores a field
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetWordSpacing")
private external fun _nSetWordSpacing(ptr: NativePointer, wordSpacing: Float)

// Runnable: refTypeface().release() is a ref++ on the typeface TextStyle keeps owning, never a dealloc
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetTypeface")
private external fun _nGetTypeface(ptr: NativePointer): NativePointer

// Native: TextStyle::setTypeface -> drops old sk_sp<SkTypeface> -> ~SkTypeface_Mac -> CFRelease(fFontRef)
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetTypeface")
@ForceNativeThreadState
private external fun _nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)

// Runnable: copies the locale SkString
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLocale")
private external fun _nGetLocale(ptr: NativePointer): NativePointer

// Native: skString -> SkString::SkString calls strlen and SkString::Rec::Make copies the locale bytes
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLocale")
@ForceNativeThreadState
private external fun _nSetLocale(ptr: NativePointer, locale: InteropPointer)

// Runnable: TextStyle::getTextBaseline returns a stored enum
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineMode")
private external fun _nGetBaselineMode(ptr: NativePointer): Int

// Runnable: TextStyle::setTextBaseline stores an enum
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineMode")
private external fun _nSetBaselineMode(ptr: NativePointer, mode: Int)

// Native: SkFont::getMetrics -> SkStrikeSpec::findOrCreateStrike -> SkTypeface_Mac::onFilterRec
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontMetrics")
@ForceNativeThreadState
private external fun _nGetFontMetrics(ptr: NativePointer, fontMetrics: InteropPointer)

// Runnable: TextStyle::isPlaceholder returns a stored flag
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nIsPlaceholder")
private external fun _nIsPlaceholder(ptr: NativePointer): Boolean

// Runnable: TextStyle::setPlaceholder sets a stored flag
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetPlaceholder")
private external fun _nSetPlaceholder(ptr: NativePointer)

// Runnable: TextStyle::getFontEdging returns a stored enum
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontEdging")
private external fun _nGetFontEdging(ptr: NativePointer): Int

// Runnable: TextStyle::setFontEdging stores an enum
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontEdging")
private external fun _nSetFontEdging(ptr: NativePointer, fontEdging: Int)

// Runnable: TextStyle::getSubpixel returns a stored flag
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetSubpixel")
private external fun _nGetSubpixel(ptr: NativePointer): Boolean

// Runnable: TextStyle::setSubpixel stores a flag
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetSubpixel")
private external fun _nSetSubpixel(ptr: NativePointer, subpixel: Boolean)

// Runnable: TextStyle::getFontHinting returns a stored enum
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontHinting")
private external fun _nGetFontHinting(ptr: NativePointer): Int

// Runnable: TextStyle::setFontHinting stores an enum
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontHinting")
private external fun _nSetFontHinting(ptr: NativePointer, fontHinting: Int)
