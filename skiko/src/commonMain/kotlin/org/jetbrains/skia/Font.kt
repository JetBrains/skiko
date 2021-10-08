package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr

class Font : Managed {
    companion object {
        internal fun makeClone(ptr: NativePointer): Font {
            Stats.onNativeCall()
            return Font(Font_nMakeClone(ptr))
        }

        init {
            staticLoad()
        }
    }

    internal constructor(ptr: NativePointer) : super(ptr, _FinalizerHolder.PTR)

    internal constructor(ptr: NativePointer, managed: Boolean) : super(ptr, _FinalizerHolder.PTR, managed)

    /**
     * Returns Font initialized with default values
     */
    constructor() : this(_nMakeDefault()) {
        Stats.onNativeCall()
    }

    /**
     * Returns Font with Typeface and default size
     *
     * @param typeface  typeface and style used to draw and measure text. Pass null for default
     */
    constructor(typeface: Typeface?) : this(_nMakeTypeface(getPtr(typeface))) {
        Stats.onNativeCall()
        reachabilityBarrier(typeface)
    }

    /**
     * @param typeface  typeface and style used to draw and measure text. Pass null for default
     * @param size      typographic size of the text
     */
    constructor(typeface: Typeface?, size: Float) : this(_nMakeTypefaceSize(getPtr(typeface), size)) {
        Stats.onNativeCall()
        reachabilityBarrier(typeface)
    }

    /**
     * Constructs Font with default values with Typeface and size in points,
     * horizontal scale, and horizontal skew. Horizontal scale emulates condensed
     * and expanded fonts. Horizontal skew emulates oblique fonts.
     *
     * @param typeface  typeface and style used to draw and measure text. Pass null for default
     * @param size      typographic size of the text
     * @param scaleX    text horizonral scale
     * @param skewX     additional shear on x-axis relative to y-axis
     */
    constructor(typeface: Typeface?, size: Float, scaleX: Float, skewX: Float) : this(
        _nMakeTypefaceSizeScaleSkew(
            getPtr(typeface), size, scaleX, skewX
        )
    ) {
        Stats.onNativeCall()
        reachabilityBarrier(typeface)
    }

    /**
     * Compares fonts, and returns true if they are equivalent.
     * May return false if Typeface has identical contents but different pointers.
     */
    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Font_nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    /**
     * If true, instructs the font manager to always hint glyphs.
     * Returned value is only meaningful if platform uses FreeType as the font manager.
     *
     * @return  true if all glyphs are hinted
     */
    var isAutoHintingForced: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsAutoHintingForced(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetAutoHintingForced(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  true if font engine may return glyphs from font bitmaps instead of from outlines
     */
    fun areBitmapsEmbedded(): Boolean {
        return try {
            Stats.onNativeCall()
            _nAreBitmapsEmbedded(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * @return  true if glyphs may be drawn at sub-pixel offsets
     */
    var isSubpixel: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsSubpixel(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetSubpixel(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  true if font and glyph metrics are requested to be linearly scalable
     */
    fun areMetricsLinear(): Boolean {
        return try {
            Stats.onNativeCall()
            _nAreMetricsLinear(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns true if bold is approximated by increasing the stroke width when creating glyph
     * bitmaps from outlines.
     *
     * @return  true if bold is approximated through stroke width
     */
    var isEmboldened: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsEmboldened(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetEmboldened(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns true if baselines will be snapped to pixel positions when the current transformation
     * matrix is axis aligned.
     *
     * @return  true if baselines may be snapped to pixels
     */
    var isBaselineSnapped: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsBaselineSnapped(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetBaselineSnapped(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Whether edge pixels draw opaque or with partial transparency.
     */
    var edging: FontEdging
        get() = try {
            Stats.onNativeCall()
            FontEdging.values().get(_nGetEdging(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetEdging(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  level of glyph outline adjustment
     */
    var hinting: FontHinting
        get() = try {
            Stats.onNativeCall()
            FontHinting.values().get(_nGetHinting(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetHinting(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns a font with the same attributes of this font, but with the specified size.
     */
    fun makeWithSize(size: Float): Font {
        return Font(typeface, size, scaleX, skewX)
    }

    /**
     * @return [Typeface] if set, or null
     */
    val typeface: Typeface?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetTypeface(_ptr)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return [Typeface] if set, or the default typeface.
     */
    val typefaceOrDefault: Typeface
        get() = try {
            Stats.onNativeCall()
            Typeface(_nGetTypefaceOrDefault(_ptr))
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  text size in points
     */
    var size: Float
        get() = try {
            Stats.onNativeCall()
            Font_nGetSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetSize(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }


    /**
     * @return  text scale on x-axis. Default value is 1
     */
    var scaleX: Float
        get() = try {
            Stats.onNativeCall()
            _nGetScaleX(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetScaleX(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  text skew on x-axis. Default value is 0
     */
    var skewX: Float
        get() = try {
            Stats.onNativeCall()
            _nGetSkewX(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            _nSetSkewX(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Sets Typeface to typeface. Pass null to use the default typeface.
     */
    fun setTypeface(typeface: Typeface?): Font {
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

    /**
     * Converts text into glyph indices.
     *
     * @return  the corresponding glyph ids for each character.
     */
    fun getStringGlyphs(s: String): ShortArray {
        return getUTF32Glyphs(s.intCodePoints())
    }

    /**
     * Given an array of UTF32 character codes, return their corresponding glyph IDs.
     *
     * @return  the corresponding glyph IDs for each character.
     */
    fun getUTF32Glyphs(uni: IntArray?): ShortArray {
        return try {
            Stats.onNativeCall()
            _nGetUTF32Glyphs(_ptr, uni)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * @return  the glyph that corresponds to the specified unicode code-point (in UTF32 encoding). If the unichar is not supported, returns 0
     */
    fun getUTF32Glyph(unichar: Int): Short {
        return try {
            Stats.onNativeCall()
            _nGetUTF32Glyph(_ptr, unichar)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * @return  number of glyphs represented by text
     */
    fun getStringGlyphsCount(s: String?): Int {
        return try {
            Stats.onNativeCall()
            _nGetStringGlyphsCount(_ptr, s)
        } finally {
            reachabilityBarrier(this)
        }
    }
    /**
     * @param p  stroke width or PathEffect may modify the advance with
     * @return   the bounding box of text
     */
    /**
     * @return  the bounding box of text
     */
    fun measureText(s: String?, p: Paint? = null): Rect {
        return try {
            Stats.onNativeCall()
            _nMeasureText(_ptr, s, getPtr(p))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(p)
        }
    }

    fun measureTextWidth(s: String?): Float {
        Stats.onNativeCall()
        return measureTextWidth(s, null)
    }

    fun measureTextWidth(s: String?, p: Paint?): Float {
        return try {
            Stats.onNativeCall()
            _nMeasureTextWidth(
                _ptr,
                s,
                getPtr(p)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(p)
        }
    }

    /**
     * Retrieves the advances for each glyph
     */
    fun getWidths(glyphs: ShortArray?): FloatArray {
        return try {
            Stats.onNativeCall()
            _nGetWidths(_ptr, glyphs)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Retrieves the bounds for each glyph
     */
    fun getBounds(glyphs: ShortArray?): Array<Rect> {
        return getBounds(glyphs, null)
    }

    /**
     * Retrieves the bounds for each glyph
     */
    fun getBounds(glyphs: ShortArray?, p: Paint?): Array<Rect> {
        return try {
            Stats.onNativeCall()
            _nGetBounds(
                _ptr,
                glyphs,
                getPtr(p)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(p)
        }
    }

    /**
     * Retrieves the positions for each glyph.
     */
    fun getPositions(glyphs: ShortArray?): Array<Point> {
        return try {
            Stats.onNativeCall()
            _nGetPositions(_ptr, glyphs, 0f, 0f)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Retrieves the positions for each glyph, beginning at the specified origin.
     */
    fun getPositions(glyphs: ShortArray?, offset: Point): Array<Point> {
        return try {
            Stats.onNativeCall()
            _nGetPositions(_ptr, glyphs, offset.x, offset.y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Retrieves the x-positions for each glyph.
     */
    fun getXPositions(glyphs: ShortArray?): FloatArray {
        return try {
            Stats.onNativeCall()
            _nGetXPositions(_ptr, glyphs, 0f)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Retrieves the x-positions for each glyph, beginning at the specified origin.
     */
    fun getXPositions(glyphs: ShortArray?, offset: Float): FloatArray {
        return try {
            Stats.onNativeCall()
            _nGetXPositions(_ptr, glyphs, offset)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * If the glyph has an outline, returns it. The glyph outline may be empty.
     * Degenerate contours in the glyph outline will be skipped. If glyph is described by a bitmap, returns null.
     */
    fun getPath(glyph: Short): Path? {
        return try {
            Stats.onNativeCall()
            val ptr = _nGetPath(_ptr, glyph)
            if (ptr == NullPointer) null else Path(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Return glyph outlines, some of which might be null.
     */
    fun getPaths(glyphs: ShortArray?): Array<Path> {
        return try {
            Stats.onNativeCall()
            _nGetPaths(_ptr, glyphs)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns FontMetrics associated with Typeface. Results are scaled by text size but does not take into account
     * dimensions required by text scale, text skew, fake bold, style stroke, and [PathEffect].
     */
    val metrics: FontMetrics
        get() = try {
            Stats.onNativeCall()
            _nGetMetrics(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns the recommended spacing between lines: the sum of metrics descent, ascent, and leading.
     * Result is scaled by text size but does not take into account dimensions required by stroking and SkPathEffect.
     */
    val spacing: Float
        get() = try {
            Stats.onNativeCall()
            _nGetSpacing(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    private object _FinalizerHolder {
        val PTR = Font_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_Font__1nGetFinalizer")
private external fun Font_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeClone")
private external fun Font_nMakeClone(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nEquals")
private external fun Font_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSize")
private external fun Font_nGetSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeDefault")
private external fun _nMakeDefault(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypeface")
private external fun _nMakeTypeface(typefacePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypefaceSize")
private external fun _nMakeTypefaceSize(typefacePtr: NativePointer, size: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypefaceSizeScaleSkew")
private external fun _nMakeTypefaceSizeScaleSkew(typefacePtr: NativePointer, size: Float, scaleX: Float, skewX: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsAutoHintingForced")
private external fun _nIsAutoHintingForced(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nAreBitmapsEmbedded")
private external fun _nAreBitmapsEmbedded(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsSubpixel")
private external fun _nIsSubpixel(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nAreMetricsLinear")
private external fun _nAreMetricsLinear(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsEmboldened")
private external fun _nIsEmboldened(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsBaselineSnapped")
private external fun _nIsBaselineSnapped(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetAutoHintingForced")
private external fun _nSetAutoHintingForced(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetBitmapsEmbedded")
private external fun _nSetBitmapsEmbedded(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSubpixel")
private external fun _nSetSubpixel(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetMetricsLinear")
private external fun _nSetMetricsLinear(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetEmboldened")
private external fun _nSetEmboldened(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetBaselineSnapped")
private external fun _nSetBaselineSnapped(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetEdging")
private external fun _nGetEdging(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetEdging")
private external fun _nSetEdging(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetHinting")
private external fun _nGetHinting(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetHinting")
private external fun _nSetHinting(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetTypeface")
private external fun _nGetTypeface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetTypefaceOrDefault")
private external fun _nGetTypefaceOrDefault(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetScaleX")
private external fun _nGetScaleX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSkewX")
private external fun _nGetSkewX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetTypeface")
private external fun _nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSize")
private external fun _nSetSize(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetScaleX")
private external fun _nSetScaleX(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSkewX")
private external fun _nSetSkewX(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetStringGlyphs")
private external fun _nGetStringGlyphs(ptr: NativePointer, str: String?): ShortArray?

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetUTF32Glyph")
private external fun _nGetUTF32Glyph(ptr: NativePointer, uni: Int): Short

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetUTF32Glyphs")
private external fun _nGetUTF32Glyphs(ptr: NativePointer, uni: IntArray?): ShortArray

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetStringGlyphsCount")
private external fun _nGetStringGlyphsCount(ptr: NativePointer, str: String?): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nMeasureText")
private external fun _nMeasureText(ptr: NativePointer, str: String?, paintPtr: NativePointer): Rect

@ExternalSymbolName("org_jetbrains_skia_Font__1nMeasureTextWidth")
private external fun _nMeasureTextWidth(ptr: NativePointer, str: String?, paintPtr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetWidths")
private external fun _nGetWidths(ptr: NativePointer, glyphs: ShortArray?): FloatArray

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetBounds")
private external fun _nGetBounds(ptr: NativePointer, glyphs: ShortArray?, paintPtr: NativePointer): Array<Rect>

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPositions")
private external fun _nGetPositions(ptr: NativePointer, glyphs: ShortArray?, x: Float, y: Float): Array<Point>

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetXPositions")
private external fun _nGetXPositions(ptr: NativePointer, glyphs: ShortArray?, x: Float): FloatArray

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPath")
private external fun _nGetPath(ptr: NativePointer, glyph: Short): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPaths")
private external fun _nGetPaths(ptr: NativePointer, glyphs: ShortArray?): Array<Path>

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetMetrics")
private external fun _nGetMetrics(ptr: NativePointer): FontMetrics

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSpacing")
private external fun _nGetSpacing(ptr: NativePointer): Float
