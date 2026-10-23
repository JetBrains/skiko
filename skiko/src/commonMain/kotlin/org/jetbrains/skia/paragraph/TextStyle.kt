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

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of the delete function without calling it, so it is short and cannot
// block.
// Native call path: deleteTextStyle
private external fun TextStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nMake")
// No ForceNativeThreadState: creates a default text style with one fixed font family, no typeface and empty paints, so
// it is short and cannot block.
// Native call path: TextStyle::TextStyle
private external fun TextStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nEquals")
// Needs ForceNativeThreadState: compares font family, shadow and feature lists element by element, so its time grows
// with their lengths.
// Native call path: TextStyle::equals
@ForceNativeThreadState
private external fun TextStyle_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontStyle")
// No ForceNativeThreadState: returns a stored font style packed into an integer, so it is short and cannot block.
// Native call path: TextStyle::getFontStyle
private external fun TextStyle_nGetFontStyle(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontStyle")
// No ForceNativeThreadState: stores a packed font style value, so it is short and cannot block.
// Native call path: TextStyle::setFontStyle
private external fun TextStyle_nSetFontStyle(ptr: NativePointer, fontStyle: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontSize")
// No ForceNativeThreadState: returns a stored font size, so it is short and cannot block.
// Native call path: TextStyle::getFontSize
private external fun TextStyle_nGetFontSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontSize")
// No ForceNativeThreadState: stores a font size, so it is short and cannot block.
// Native call path: TextStyle::setFontSize
private external fun TextStyle_nSetFontSize(ptr: NativePointer, size: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies")
// Needs ForceNativeThreadState: builds a new string wrapper for every font family, so its time grows with the number of
// families.
// Native call path: TextStyle::getFontFamilies
@ForceNativeThreadState
private external fun TextStyle_nGetFontFamilies(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetHeight")
// No ForceNativeThreadState: returns a stored height override or height value, so it is short and cannot block.
// Native call path: TextStyle::getHeight
private external fun TextStyle_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetHeight")
// No ForceNativeThreadState: stores a height override or height value, so it is short and cannot block.
// Native call path: TextStyle::setHeight
private external fun TextStyle_nSetHeight(ptr: NativePointer, override: Boolean, height: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetHalfLeading")
// No ForceNativeThreadState: reads a stored half-leading flag, so it is short and cannot block.
// Native call path: TextStyle::getHalfLeading
private external fun TextStyle_nGetHalfLeading(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetHalfLeading")
// No ForceNativeThreadState: stores a half-leading flag, so it is short and cannot block.
// Native call path: TextStyle::setHalfLeading
private external fun TextStyle_nSetHalfLeading(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetTopRatio")
// No ForceNativeThreadState: returns a stored top ratio, so it is short and cannot block.
// Native call path: TextStyle::getTopRatio
private external fun TextStyle_nGetTopRatio(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetTopRatio")
// No ForceNativeThreadState: stores a top ratio, so it is short and cannot block.
// Native call path: TextStyle::setTopRatio
private external fun TextStyle_nSetTopRatio(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineShift")
// No ForceNativeThreadState: returns a stored baseline shift, so it is short and cannot block.
// Native call path: TextStyle::getBaselineShift
private external fun TextStyle_nGetBaselineShift(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineShift")
// No ForceNativeThreadState: stores a baseline shift, so it is short and cannot block.
// Native call path: TextStyle::setBaselineShift
private external fun TextStyle_nSetBaselineShift(ptr: NativePointer, baselineShift: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAttributeEquals")
// Needs ForceNativeThreadState: compares font and shadow lists element by element, so its time grows with how many
// entries they hold.
// Native call path: TextStyle::equalsByFonts -> TextStyle::matchOneAttribute
@ForceNativeThreadState
private external fun _nAttributeEquals(ptr: NativePointer, attribute: Int, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetColor")
// No ForceNativeThreadState: returns a stored color value, so it is short and cannot block.
// Native call path: TextStyle::getColor
private external fun _nGetColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetColor")
// No ForceNativeThreadState: stores a color value, so it is short and cannot block.
// Native call path: TextStyle::setColor
private external fun _nSetColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetForeground")
// No ForceNativeThreadState: copies the foreground paint, which only adds references that TextStyle keeps, so it is
// short and cannot block.
// Native call path: TextStyle::getForeground
private external fun _nGetForeground(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetForeground")
// Needs ForceNativeThreadState: replacing the foreground paint can free the last reference to a shader or image filter,
// which may own a Metal texture.
// Native call path: TextStyle::setForegroundPaint -> ~SkPaint
@ForceNativeThreadState
private external fun _nSetForeground(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBackground")
// No ForceNativeThreadState: copies the background paint, which only adds references that TextStyle keeps, so it is
// short and cannot block.
// Native call path: TextStyle::getBackground
private external fun _nGetBackground(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBackground")
// Needs ForceNativeThreadState: replacing the background paint can free the last reference to a shader or image filter,
// which may own a Metal texture.
// Native call path: TextStyle::setBackgroundPaint -> ~SkPaint
@ForceNativeThreadState
private external fun _nSetBackground(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle")
// No ForceNativeThreadState: copies a small plain decoration record into the result array, so it is short and cannot
// block.
// Native call path: TextStyle::getDecoration
private external fun _nGetDecorationStyle(ptr: NativePointer, decorationStyle: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetDecorationStyle")
// No ForceNativeThreadState: stores plain decoration fields, so it is short and cannot block.
// Native call path: TextStyle::setDecoration
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
// Needs ForceNativeThreadState: copies the whole shadow list just to count it, so its time grows with the number of
// shadows.
// Native call path: TextStyle::getShadows
@ForceNativeThreadState
private external fun _nGetShadowsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetShadows")
// Needs ForceNativeThreadState: copies every shadow and then converts each one to integers, so its time grows with the
// number of shadows.
// Native call path: TextStyle::getShadows
@ForceNativeThreadState
private external fun _nGetShadows(ptr: NativePointer, res: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddShadow")
// Needs ForceNativeThreadState: adding a shadow can make the list grow and relocate all existing shadows, so its time
// grows with how many there are.
// Native call path: TextStyle::addShadow -> std::vector::emplace_back
@ForceNativeThreadState
private external fun _nAddShadow(ptr: NativePointer, color: Int, offsetX: Float, offsetY: Float, blurSigma: Double)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearShadows")
// Needs ForceNativeThreadState: clears the shadow list, so its time grows with the shadow count.
// Native call path: TextStyle::resetShadows -> std::vector::clear
@ForceNativeThreadState
private external fun _nClearShadows(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeatures")
// Needs ForceNativeThreadState: copies every font feature and then loops over all of them to fill the array, so its
// time grows with the feature count.
// Native call path: TextStyle::getFontFeatures -> skija::FontFeature::writeToIntArray
@ForceNativeThreadState
private external fun _nGetFontFeatures(ptr: NativePointer, resultIntsArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeaturesSize")
// Needs ForceNativeThreadState: copies the whole font feature list just to count it, so its time grows with the feature
// count.
// Native call path: TextStyle::getFontFeatures
@ForceNativeThreadState
private external fun _nGetFontFeaturesSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature")
// Needs ForceNativeThreadState: adding a font feature can grow the list and relocate every existing feature, so its
// time grows with the feature count.
// Native call path: TextStyle::addFontFeature -> std::vector::emplace_back
@ForceNativeThreadState
private external fun _nAddFontFeature(ptr: NativePointer, name: InteropPointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearFontFeatures")
// Needs ForceNativeThreadState: clearing the font feature list destroys every feature and its string, so its time grows
// with the feature count.
// Native call path: TextStyle::resetFontFeatures -> std::vector::clear
@ForceNativeThreadState
private external fun _nClearFontFeatures(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontFamilies")
// Needs ForceNativeThreadState: copies every font family name and destroys the old list, so its time grows with the
// number and length of names.
// Native call path: skStringVector -> skString -> SkString::SkString
@ForceNativeThreadState
private external fun _nSetFontFamilies(ptr: NativePointer, families: InteropPointer, familiesSize: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLetterSpacing")
// No ForceNativeThreadState: returns a stored letter spacing value, so it is short and cannot block.
// Native call path: TextStyle::getLetterSpacing
private external fun _nGetLetterSpacing(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLetterSpacing")
// No ForceNativeThreadState: stores a letter spacing value, so it is short and cannot block.
// Native call path: TextStyle::setLetterSpacing
private external fun _nSetLetterSpacing(ptr: NativePointer, letterSpacing: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetWordSpacing")
// No ForceNativeThreadState: returns a stored word spacing value, so it is short and cannot block.
// Native call path: TextStyle::getWordSpacing
private external fun _nGetWordSpacing(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetWordSpacing")
// No ForceNativeThreadState: stores a word spacing value, so it is short and cannot block.
// Native call path: TextStyle::setWordSpacing
private external fun _nSetWordSpacing(ptr: NativePointer, wordSpacing: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetTypeface")
// No ForceNativeThreadState: adds a reference to the typeface the style already owns, which never frees it, so it is
// short and cannot block.
// Native call path: TextStyle::refTypeface
private external fun _nGetTypeface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetTypeface")
// Needs ForceNativeThreadState: replacing the typeface can free the last reference to the old one, which releases a
// CoreText font.
// Native call path: TextStyle::setTypeface -> ~SkTypeface_Mac -> CFRelease
@ForceNativeThreadState
private external fun _nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLocale")
// No ForceNativeThreadState: copies the stored locale string into a new string, so it is short and cannot block.
// Native call path: TextStyle::getLocale
private external fun _nGetLocale(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLocale")
// Needs ForceNativeThreadState: scans and copies the locale string, so its time grows with the length of the locale.
// Native call path: skString -> SkString::SkString -> SkString::Rec::Make
@ForceNativeThreadState
private external fun _nSetLocale(ptr: NativePointer, locale: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineMode")
// No ForceNativeThreadState: returns a stored baseline mode, so it is short and cannot block.
// Native call path: TextStyle::getTextBaseline
private external fun _nGetBaselineMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineMode")
// No ForceNativeThreadState: stores a baseline mode, so it is short and cannot block.
// Native call path: TextStyle::setTextBaseline
private external fun _nSetBaselineMode(ptr: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontMetrics")
// Needs ForceNativeThreadState: takes a font-cache lock and uses CoreText/CoreGraphics to obtain font metrics.
// Native call path: SkFont::getMetrics -> SkStrikeSpec::findOrCreateStrike -> SkTypeface_Mac::onFilterRec
@ForceNativeThreadState
private external fun _nGetFontMetrics(ptr: NativePointer, fontMetrics: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nIsPlaceholder")
// No ForceNativeThreadState: only returns a stored flag, so it is short and cannot block.
// Native call path: TextStyle::isPlaceholder
private external fun _nIsPlaceholder(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetPlaceholder")
// No ForceNativeThreadState: only stores a flag, so it is short and cannot block.
// Native call path: TextStyle::setPlaceholder
private external fun _nSetPlaceholder(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontEdging")
// No ForceNativeThreadState: only returns a stored enum, so it is short and cannot block.
// Native call path: TextStyle::getFontEdging
private external fun _nGetFontEdging(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontEdging")
// No ForceNativeThreadState: only stores an enum, so it is short and cannot block.
// Native call path: TextStyle::setFontEdging
private external fun _nSetFontEdging(ptr: NativePointer, fontEdging: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetSubpixel")
// No ForceNativeThreadState: only returns a stored flag, so it is short and cannot block.
// Native call path: TextStyle::getSubpixel
private external fun _nGetSubpixel(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetSubpixel")
// No ForceNativeThreadState: only stores a flag, so it is short and cannot block.
// Native call path: TextStyle::setSubpixel
private external fun _nSetSubpixel(ptr: NativePointer, subpixel: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontHinting")
// No ForceNativeThreadState: only returns a stored enum, so it is short and cannot block.
// Native call path: TextStyle::getFontHinting
private external fun _nGetFontHinting(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontHinting")
// No ForceNativeThreadState: only stores an enum, so it is short and cannot block.
// Native call path: TextStyle::setFontHinting
private external fun _nSetFontHinting(ptr: NativePointer, fontHinting: Int)
