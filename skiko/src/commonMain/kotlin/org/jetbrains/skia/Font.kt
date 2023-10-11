package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.withResult

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
    override fun nativeEquals(other: Native?): Boolean {
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
     * If true, requests, but does not require, to use bitmaps in fonts instead of outlines.
     */
    fun setBitmapsEmbedded(value: Boolean) {
        try {
            Stats.onNativeCall()
            _nSetBitmapsEmbedded(_ptr, value)
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
            reachabilityBarrier(this)
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
            if (uni == null) {
                shortArrayOf()
            } else {
                withResult(ShortArray(uni.size)) {
                    _nGetUTF32Glyphs(_ptr, toInterop(uni), uni.size, it)
                }
            }
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
            interopScope { _nGetStringGlyphsCount(_ptr, toInterop(s), s?.length ?: 0) }
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
            Rect.fromInteropPointer() {
                _nMeasureText(_ptr, toInterop(s), s?.length ?: 0, getPtr(p), it)
            }
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
            interopScope {
                _nMeasureTextWidth(
                    _ptr,
                    toInterop(s),
                    s?.length ?: 0,
                    getPtr(p)
                )
            }

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
            if (glyphs == null) {
                return floatArrayOf()
            } else {
                withResult(FloatArray(glyphs.size)) {
                    _nGetWidths(_ptr, toInterop(glyphs), glyphs.size, it)
                }
            }
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

            if (glyphs == null) {
                emptyArray()
            } else {
                Rect.fromInteropPointer(glyphs.size * 4) {
                    _nGetBounds(
                        _ptr,
                        toInterop(glyphs),
                        glyphs.size,
                        getPtr(p),
                        it
                    )
                }
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(p)
        }
    }

    /**
     * Retrieves the positions for each glyph.
     */
    fun getPositions(glyphs: ShortArray?): Array<Point> {
        return getPositions(glyphs, Point(0f, 0f))
    }

    /**
     * Retrieves the positions for each glyph, beginning at the specified origin.
     */
    fun getPositions(glyphs: ShortArray?, offset: Point): Array<Point> {
        return try {
            Stats.onNativeCall()
            if (glyphs == null) {
                emptyArray()
            } else {
                val positionsData = withResult(FloatArray(glyphs.size * 2)) {
                    _nGetPositions(_ptr, toInterop(glyphs), glyphs.size, offset.x, offset.y, it)
                }
                (0 until glyphs.size).map { i ->
                    Point(positionsData[2*i], positionsData[2*i + 1])
                }.toTypedArray()
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Retrieves the x-positions for each glyph, beginning at the specified origin.
     */
    fun getXPositions(glyphs: ShortArray?): FloatArray {
        return getXPositions(glyphs, 0f)
    }

    /**
     * Retrieves the x-positions for each glyph, beginning at the specified origin.
     */
    fun getXPositions(glyphs: ShortArray?, offset: Float): FloatArray {
        return try {
            Stats.onNativeCall()
            if (glyphs == null) {
                floatArrayOf()
            } else {
                withResult(FloatArray(glyphs.size)) {
                    _nGetXPositions(_ptr, toInterop(glyphs), offset, glyphs.size, it)
                }
            }
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
            arrayDecoderScope({
                ArrayDecoder(
                    interopScope { _nGetPaths(_ptr, toInterop(glyphs), glyphs?.size ?: 0) }, Path_nGetFinalizer()
                )
            }) { arrayDecoder ->
                (0 until arrayDecoder.size).map { i->
                    Path(arrayDecoder.release(i))
                }.toTypedArray()
            }
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
            FontMetrics.fromInteropPointer {
                _nGetMetrics(_ptr, it)
            }
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
            _nGetSpacing(_ptr, NullPointer)
        } finally {
            reachabilityBarrier(this)
        }

    private object _FinalizerHolder {
        val PTR = Font_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_Font__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetFinalizer")
private external fun Font_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeClone")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nMakeClone")
private external fun Font_nMakeClone(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nEquals")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nEquals")
private external fun Font_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetSize")
private external fun Font_nGetSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeDefault")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nMakeDefault")
private external fun _nMakeDefault(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nMakeTypeface")
private external fun _nMakeTypeface(typefacePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypefaceSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nMakeTypefaceSize")
private external fun _nMakeTypefaceSize(typefacePtr: NativePointer, size: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypefaceSizeScaleSkew")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nMakeTypefaceSizeScaleSkew")
private external fun _nMakeTypefaceSizeScaleSkew(typefacePtr: NativePointer, size: Float, scaleX: Float, skewX: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsAutoHintingForced")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nIsAutoHintingForced")
private external fun _nIsAutoHintingForced(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nAreBitmapsEmbedded")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nAreBitmapsEmbedded")
private external fun _nAreBitmapsEmbedded(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsSubpixel")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nIsSubpixel")
private external fun _nIsSubpixel(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nAreMetricsLinear")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nAreMetricsLinear")
private external fun _nAreMetricsLinear(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsEmboldened")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nIsEmboldened")
private external fun _nIsEmboldened(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsBaselineSnapped")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nIsBaselineSnapped")
private external fun _nIsBaselineSnapped(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetAutoHintingForced")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetAutoHintingForced")
private external fun _nSetAutoHintingForced(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetBitmapsEmbedded")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetBitmapsEmbedded")
private external fun _nSetBitmapsEmbedded(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSubpixel")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetSubpixel")
private external fun _nSetSubpixel(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetMetricsLinear")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetMetricsLinear")
private external fun _nSetMetricsLinear(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetEmboldened")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetEmboldened")
private external fun _nSetEmboldened(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetBaselineSnapped")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetBaselineSnapped")
private external fun _nSetBaselineSnapped(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetEdging")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetEdging")
private external fun _nGetEdging(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetEdging")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetEdging")
private external fun _nSetEdging(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetHinting")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetHinting")
private external fun _nGetHinting(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetHinting")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetHinting")
private external fun _nSetHinting(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetTypeface")
private external fun _nGetTypeface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetTypefaceOrDefault")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetTypefaceOrDefault")
private external fun _nGetTypefaceOrDefault(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetScaleX")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetScaleX")
private external fun _nGetScaleX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSkewX")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetSkewX")
private external fun _nGetSkewX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetTypeface")
private external fun _nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetSize")
private external fun _nSetSize(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetScaleX")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetScaleX")
private external fun _nSetScaleX(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSkewX")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nSetSkewX")
private external fun _nSetSkewX(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetUTF32Glyph")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetUTF32Glyph")
private external fun _nGetUTF32Glyph(ptr: NativePointer, uni: Int): Short

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetUTF32Glyphs")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetUTF32Glyphs")
private external fun _nGetUTF32Glyphs(ptr: NativePointer, uni: InteropPointer, uniArrLen: Int, resultGlyphs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetStringGlyphsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetStringGlyphsCount")
private external fun _nGetStringGlyphsCount(ptr: NativePointer, str: InteropPointer, len: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nMeasureText")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nMeasureText")
private external fun _nMeasureText(ptr: NativePointer, str: InteropPointer, len: Int, paintPtr: NativePointer, rect: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nMeasureTextWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nMeasureTextWidth")
private external fun _nMeasureTextWidth(ptr: NativePointer, str: InteropPointer, len: Int, paintPtr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetWidths")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetWidths")
private external fun _nGetWidths(ptr: NativePointer, glyphs: InteropPointer, count: Int, width: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetBounds")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetBounds")
private external fun _nGetBounds(ptr: NativePointer, glyphs: InteropPointer, count: Int, paintPtr: NativePointer, bounds: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPositions")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetPositions")
private external fun _nGetPositions(ptr: NativePointer, glyphs: InteropPointer, count: Int, x: Float, y: Float, positions: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetXPositions")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetXPositions")
private external fun _nGetXPositions(ptr: NativePointer, glyphs: InteropPointer, x: Float, count: Int, positions: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetPath")
private external fun _nGetPath(ptr: NativePointer, glyph: Short): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPaths")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetPaths")
private external fun _nGetPaths(ptr: NativePointer, glyphs: InteropPointer, count: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetMetrics")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetMetrics")
private external fun _nGetMetrics(ptr: NativePointer, metrics: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSpacing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Font__1nGetSpacing")
private external fun _nGetSpacing(ptr: NativePointer, glyphsArr: NativePointer): Float
