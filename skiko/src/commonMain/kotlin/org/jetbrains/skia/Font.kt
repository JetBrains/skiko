package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
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
    constructor() : this(Font_nMakeDefault()) {
        Stats.onNativeCall()
    }

    /**
     * Returns Font with Typeface and default size
     *
     * @param typeface  typeface and style used to draw and measure text. Pass null for default
     */
    constructor(typeface: Typeface?) : this(Font_nMakeTypeface(getPtr(typeface))) {
        Stats.onNativeCall()
        reachabilityBarrier(typeface)
    }

    /**
     * @param typeface  typeface and style used to draw and measure text. Pass null for default
     * @param size      typographic size of the text
     */
    constructor(typeface: Typeface?, size: Float) : this(Font_nMakeTypefaceSize(getPtr(typeface), size)) {
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
        Font_nMakeTypefaceSizeScaleSkew(
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
            Font_nIsAutoHintingForced(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Font_nSetAutoHintingForced(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  true if font engine may return glyphs from font bitmaps instead of from outlines
     */
    fun areBitmapsEmbedded(): Boolean {
        return try {
            Stats.onNativeCall()
            Font_nAreBitmapsEmbedded(_ptr)
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
            Font_nSetBitmapsEmbedded(_ptr, value)
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
            Font_nIsSubpixel(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Font_nSetSubpixel(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns true if font and glyph metrics are requested to be linearly scalable.
     *
     * @return  true if font and glyph metrics are requested to be linearly scalable.
     */
    var isLinearMetrics: Boolean
        get() = try {
            Stats.onNativeCall()
            Font_nIsLinearMetrics(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            try {
                Stats.onNativeCall()
                Font_nSetLinearMetrics(_ptr, value)
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
            Font_nIsEmboldened(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Font_nSetEmboldened(_ptr, value)
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
            Font_nIsBaselineSnapped(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Font_nSetBaselineSnapped(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Whether edge pixels draw opaque or with partial transparency.
     */
    var edging: FontEdging
        get() = try {
            Stats.onNativeCall()
            FontEdging.values().get(Font_nGetEdging(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Font_nSetEdging(_ptr, value.ordinal)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  level of glyph outline adjustment
     */
    var hinting: FontHinting
        get() = try {
            Stats.onNativeCall()
            FontHinting.values().get(Font_nGetHinting(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Font_nSetHinting(_ptr, value.ordinal)
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
            val ptr = Font_nGetTypeface(_ptr)
            if (ptr == NullPointer) null else Typeface(ptr)
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
            Font_nSetSize(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }


    /**
     * @return  text scale on x-axis. Default value is 1
     */
    var scaleX: Float
        get() = try {
            Stats.onNativeCall()
            Font_nGetScaleX(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Font_nSetScaleX(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  text skew on x-axis. Default value is 0
     */
    var skewX: Float
        get() = try {
            Stats.onNativeCall()
            Font_nGetSkewX(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            Font_nSetSkewX(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Sets Typeface to typeface. Pass null to use the default typeface.
     */
    fun setTypeface(typeface: Typeface?): Font {
        return try {
            Stats.onNativeCall()
            Font_nSetTypeface(
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
        return getUTF32Glyphs(s.codePointsAsIntArray)
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
                    Font_nGetUTF32Glyphs(_ptr, toInterop(uni), uni.size, it)
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
            Font_nGetUTF32Glyph(_ptr, unichar)
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
            interopScope { Font_nGetStringGlyphsCount(_ptr, toInterop(s), s?.length ?: 0) }
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
                Font_nMeasureText(_ptr, toInterop(s), s?.length ?: 0, getPtr(p), it)
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
                Font_nMeasureTextWidth(
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
                    Font_nGetWidths(_ptr, toInterop(glyphs), glyphs.size, it)
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
                    Font_nGetBounds(
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
                    Font_nGetPositions(_ptr, toInterop(glyphs), glyphs.size, offset.x, offset.y, it)
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
                    Font_nGetXPositions(_ptr, toInterop(glyphs), offset, glyphs.size, it)
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
            val ptr = Font_nGetPath(_ptr, glyph)
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
                    interopScope { Font_nGetPaths(_ptr, toInterop(glyphs), glyphs?.size ?: 0) }, Path_nGetFinalizer()
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
                Font_nGetMetrics(_ptr, it)
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
            Font_nGetSpacing(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    private object _FinalizerHolder {
        val PTR = Font_nGetFinalizer()
    }
}