package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import kotlin.jvm.JvmStatic

class Font : Managed {
    companion object {
        @JvmStatic
        internal fun makeClone(ptr: NativePointer): Font = nativeCallFromStatic { Font(Font_nMakeClone(ptr)) }

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
    constructor(typeface: Typeface?) : this(_nMakeTypeface(typeface.pointerOrNullPointer)) {
        Stats.onNativeCall()
        reachabilityBarrier(typeface)
    }

    /**
     * @param typeface  typeface and style used to draw and measure text. Pass null for default
     * @param size      typographic size of the text
     */
    constructor(typeface: Typeface?, size: Float) : this(_nMakeTypefaceSize(typeface.pointerOrNullPointer, size)) {
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
            typeface.pointerOrNullPointer, size, scaleX, skewX
        )
    ) {
        Stats.onNativeCall()
        reachabilityBarrier(typeface)
    }

    /**
     * Compares fonts, and returns true if they are equivalent.
     * May return false if Typeface has identical contents but different pointers.
     */
    override fun nativeEquals(other: Native?): Boolean = nativeCall(other) { other ->
        Font_nEquals(_ptr, other.pointerOrNullPointer)
    }

    /**
     * If true, instructs the font manager to always hint glyphs.
     * Returned value is only meaningful if platform uses FreeType as the font manager.
     *
     * @return  true if all glyphs are hinted
     */
    var isAutoHintingForced: Boolean
        get() = nativeCall { _nIsAutoHintingForced(_ptr) }
        set(value) = nativeCall { _nSetAutoHintingForced(_ptr, value) }

    /**
     * @return  true if font engine may return glyphs from font bitmaps instead of from outlines
     */
    fun areBitmapsEmbedded(): Boolean = nativeCall { _nAreBitmapsEmbedded(_ptr) }

    /**
     * If true, requests, but does not require, to use bitmaps in fonts instead of outlines.
     */
    fun setBitmapsEmbedded(value: Boolean) = nativeCall { _nSetBitmapsEmbedded(_ptr, value) }

    /**
     * @return  true if glyphs may be drawn at sub-pixel offsets
     */
    var isSubpixel: Boolean
        get () = nativeCall { _nIsSubpixel(_ptr) }
        set(value) = nativeCall { _nSetSubpixel(_ptr, value) }

    /**
     * Returns true if font and glyph metrics are requested to be linearly scalable.
     *
     * @return  true if font and glyph metrics are requested to be linearly scalable.
     */
    var isLinearMetrics: Boolean
        get () = nativeCall { _nIsLinearMetrics(_ptr) }
        set(value) = nativeCall { _nSetLinearMetrics(_ptr, value) }

    /**
     * Returns true if bold is approximated by increasing the stroke width when creating glyph
     * bitmaps from outlines.
     *
     * @return  true if bold is approximated through stroke width
     */
    var isEmboldened: Boolean
        get() = nativeCall { _nIsEmboldened(_ptr) }
        set(value) = nativeCall { _nSetEmboldened(_ptr, value) }

    /**
     * Returns true if baselines will be snapped to pixel positions when the current transformation
     * matrix is axis aligned.
     *
     * @return  true if baselines may be snapped to pixels
     */
    var isBaselineSnapped: Boolean
        get () = nativeCall { _nIsBaselineSnapped(_ptr) }
        set(value) = nativeCall { _nSetBaselineSnapped(_ptr, value) }

    /**
     * Whether edge pixels draw opaque or with partial transparency.
     */
    var edging: FontEdging
        get() = nativeCall { FontEdging.entries[_nGetEdging(_ptr)] }
        set(value) = nativeCall { _nSetEdging(_ptr, value.ordinal) }

    /**
     * @return  level of glyph outline adjustment
     */
    var hinting: FontHinting
        get() = nativeCall { FontHinting.entries[_nGetHinting(_ptr)] }
        set(value) = nativeCall { _nSetHinting(_ptr, value.ordinal) }

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
        get() = nativeCall {
            val ptr = _nGetTypeface(_ptr)
            if (ptr == NullPointer) null else Typeface(ptr)
        }

    /**
     * @return  text size in points
     */
    var size: Float
        get () = nativeCall { Font_nGetSize(_ptr) }
        set(value) = nativeCall { _nSetSize(_ptr, value) }


    /**
     * @return  text scale on x-axis. Default value is 1
     */
    var scaleX: Float
        get() = nativeCall { _nGetScaleX(_ptr) }
        set(value) = nativeCall { _nSetScaleX(_ptr, value) }

    /**
     * @return  text skew on x-axis. Default value is 0
     */
    var skewX: Float
        get() = nativeCall { _nGetSkewX(_ptr) }
        set(value) = nativeCall { _nSetSkewX(_ptr, value) }

    /**
     * Sets Typeface to typeface. Pass null to use the default typeface.
     */
    fun setTypeface(typeface: Typeface?): Font = nativeCall(typeface) { typeface ->
        _nSetTypeface(_ptr, typeface.pointerOrNullPointer)
        this
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
    fun getUTF32Glyphs(uni: IntArray?): ShortArray = nativeCall {
        if (uni == null) {
            shortArrayOf()
        } else {
            withResult(ShortArray(uni.size)) {
                _nGetUTF32Glyphs(_ptr, toInterop(uni), uni.size, it)
            }
        }
    }

    /**
     * @return  the glyph that corresponds to the specified unicode code-point (in UTF32 encoding). If the unichar is not supported, returns 0
     */
    fun getUTF32Glyph(unichar: Int): Short = nativeCall { _nGetUTF32Glyph(_ptr, unichar) }

    /**
     * @return  number of glyphs represented by text
     */
    fun getStringGlyphsCount(s: String?): Int = nativeCall {
        interopScope { _nGetStringGlyphsCount(_ptr, toInterop(s), s?.length ?: 0) }
    }
    /**
     * @param p  stroke width or PathEffect may modify the advance with
     * @return   the bounding box of text
     */
    /**
     * @return  the bounding box of text
     */
    fun measureText(s: String?, p: Paint? = null): Rect = nativeCall(p) { p ->
        Rect.fromInteropPointer() {
            _nMeasureText(_ptr, toInterop(s), s?.length ?: 0, p.pointerOrNullPointer, it)
        }
    }

    fun measureTextWidth(s: String?): Float = measureTextWidth(s, null)

    fun measureTextWidth(s: String?, p: Paint?): Float = nativeCall(p) { p ->
        interopScope {
            _nMeasureTextWidth(
                _ptr,
                toInterop(s),
                s?.length ?: 0,
                p.pointerOrNullPointer
            )
        }
    }

    /**
     * Retrieves the advances for each glyph
     */
    fun getWidths(glyphs: ShortArray?): FloatArray = nativeCall {
        if (glyphs == null) {
            floatArrayOf()
        } else {
            withResult(FloatArray(glyphs.size)) {
                _nGetWidths(_ptr, toInterop(glyphs), glyphs.size, it)
            }
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
    fun getBounds(glyphs: ShortArray?, p: Paint?): Array<Rect> = nativeCall(p) { p ->
        if (glyphs == null) {
            emptyArray()
        } else {
            Rect.fromInteropPointer(glyphs.size * 4) {
                _nGetBounds(
                    _ptr,
                    toInterop(glyphs),
                    glyphs.size,
                    p.pointerOrNullPointer,
                    it
                )
            }
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
    fun getPositions(glyphs: ShortArray?, offset: Point): Array<Point> = nativeCall {
        if (glyphs == null) {
            emptyArray()
        } else {
            val positionsData = withResult(FloatArray(glyphs.size * 2)) {
                _nGetPositions(_ptr, toInterop(glyphs), glyphs.size, offset.x, offset.y, it)
            }
            glyphs.indices.map { i ->
                Point(positionsData[2*i], positionsData[2*i + 1])
            }.toTypedArray()
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
    fun getXPositions(glyphs: ShortArray?, offset: Float): FloatArray = nativeCall {
        if (glyphs == null) {
            floatArrayOf()
        } else {
            withResult(FloatArray(glyphs.size)) {
                _nGetXPositions(_ptr, toInterop(glyphs), offset, glyphs.size, it)
            }
        }
    }

    /**
     * If the glyph has an outline, returns it. The glyph outline may be empty.
     * Degenerate contours in the glyph outline will be skipped. If glyph is described by a bitmap, returns null.
     */
    fun getPath(glyph: Short): Path? = nativeCall {
        val ptr = _nGetPath(_ptr, glyph)
        if (ptr == NullPointer) null else Path(ptr)
    }

    /**
     * Return glyph outlines, some of which might be null.
     */
    fun getPaths(glyphs: ShortArray?): Array<Path> = nativeCall {
        arrayDecoderScope({
            ArrayDecoder(
                interopScope { _nGetPaths(_ptr, toInterop(glyphs), glyphs?.size ?: 0) }, Path_nGetFinalizer()
            )
        }) { arrayDecoder ->
            (0 until arrayDecoder.size).map { i->
                Path(arrayDecoder.release(i))
            }.toTypedArray()
        }
    }

    /**
     * Returns FontMetrics associated with Typeface. Results are scaled by text size but does not take into account
     * dimensions required by text scale, text skew, fake bold, style stroke, and [PathEffect].
     */
    val metrics: FontMetrics
        get() = nativeCall { FontMetrics.fromInteropPointer { _nGetMetrics(_ptr, it) } }

    /**
     * Returns the recommended spacing between lines: the sum of metrics descent, ascent, and leading.
     * Result is scaled by text size but does not take into account dimensions required by stroking and SkPathEffect.
     */
    val spacing: Float
        get() = nativeCall { _nGetSpacing(_ptr) }

    private object _FinalizerHolder {
        val PTR = Font_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of a delete function without calling it, so it is short and cannot
// block.
// Native call path: _nGetFinalizer
private external fun Font_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeClone")
// No ForceNativeThreadState: copies the font settings and adds one reference to its typeface, so it is short and cannot
// block.
// Native call path: SkFont::SkFont
private external fun Font_nMakeClone(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nEquals")
// No ForceNativeThreadState: compares the typeface pointer and scalar fields of two fonts, so it is short and cannot
// block.
// Native call path: SkFont::operator==
private external fun Font_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSize")
// No ForceNativeThreadState: returns a stored size field, so it is short and cannot block.
// Native call path: SkFont::getSize
private external fun Font_nGetSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeDefault")
// Needs ForceNativeThreadState: can wait for another thread still initializing the shared empty typeface on first use.
// Native call path: SkFont::SkFont -> SkTypeface::MakeEmpty -> SkEmptyTypeface::Make
@ForceNativeThreadState
private external fun _nMakeDefault(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypeface")
// Needs ForceNativeThreadState: can wait for another thread still initializing the shared empty typeface on first use.
// Native call path: SkFont::SkFont -> SkTypeface::MakeEmpty -> SkEmptyTypeface::Make
@ForceNativeThreadState
private external fun _nMakeTypeface(typefacePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypefaceSize")
// Needs ForceNativeThreadState: can wait for another thread still initializing the shared empty typeface on first use.
// Native call path: SkFont::SkFont -> SkTypeface::MakeEmpty -> SkEmptyTypeface::Make
@ForceNativeThreadState
private external fun _nMakeTypefaceSize(typefacePtr: NativePointer, size: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nMakeTypefaceSizeScaleSkew")
// Needs ForceNativeThreadState: can wait for another thread still initializing the shared empty typeface on first use.
// Native call path: SkFont::SkFont -> SkTypeface::MakeEmpty -> SkEmptyTypeface::Make
@ForceNativeThreadState
private external fun _nMakeTypefaceSizeScaleSkew(typefacePtr: NativePointer, size: Float, scaleX: Float, skewX: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsAutoHintingForced")
// No ForceNativeThreadState: reads one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::isForceAutoHinting
private external fun _nIsAutoHintingForced(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nAreBitmapsEmbedded")
// No ForceNativeThreadState: reads one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::isEmbeddedBitmaps
private external fun _nAreBitmapsEmbedded(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsSubpixel")
// No ForceNativeThreadState: reads one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::isSubpixel
private external fun _nIsSubpixel(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsLinearMetrics")
// No ForceNativeThreadState: reads one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::isLinearMetrics
private external fun _nIsLinearMetrics(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsEmboldened")
// No ForceNativeThreadState: reads one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::isEmbolden
private external fun _nIsEmboldened(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nIsBaselineSnapped")
// No ForceNativeThreadState: reads one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::isBaselineSnap
private external fun _nIsBaselineSnapped(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetAutoHintingForced")
// No ForceNativeThreadState: sets one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::setForceAutoHinting -> set_clear_mask
private external fun _nSetAutoHintingForced(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetBitmapsEmbedded")
// No ForceNativeThreadState: sets one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::setEmbeddedBitmaps -> set_clear_mask
private external fun _nSetBitmapsEmbedded(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSubpixel")
// No ForceNativeThreadState: sets one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::setSubpixel -> set_clear_mask
private external fun _nSetSubpixel(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetLinearMetrics")
// No ForceNativeThreadState: sets one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::setLinearMetrics -> set_clear_mask
private external fun _nSetLinearMetrics(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetEmboldened")
// No ForceNativeThreadState: sets one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::setEmbolden -> set_clear_mask
private external fun _nSetEmboldened(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetBaselineSnapped")
// No ForceNativeThreadState: sets one flag bit of the font, so it is short and cannot block.
// Native call path: SkFont::setBaselineSnap -> set_clear_mask
private external fun _nSetBaselineSnapped(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetEdging")
// No ForceNativeThreadState: returns a stored edging setting, so it is short and cannot block.
// Native call path: SkFont::getEdging
private external fun _nGetEdging(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetEdging")
// No ForceNativeThreadState: stores the edging setting, so it is short and cannot block.
// Native call path: SkFont::setEdging
private external fun _nSetEdging(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetHinting")
// No ForceNativeThreadState: returns a stored hinting setting, so it is short and cannot block.
// Native call path: SkFont::getHinting
private external fun _nGetHinting(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetHinting")
// No ForceNativeThreadState: stores the hinting setting, so it is short and cannot block.
// Native call path: SkFont::setHinting
private external fun _nSetHinting(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetTypeface")
// No ForceNativeThreadState: returns a new reference to the stored typeface without freeing anything, so it is short
// and cannot block.
// Native call path: SkFont::refTypeface -> sk_sp::release
private external fun _nGetTypeface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetScaleX")
// No ForceNativeThreadState: returns the stored horizontal scale value, so it is short and cannot block.
// Native call path: SkFont::getScaleX
private external fun _nGetScaleX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSkewX")
// No ForceNativeThreadState: returns the stored skew value, so it is short and cannot block.
// Native call path: SkFont::getSkewX
private external fun _nGetSkewX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetTypeface")
// Needs ForceNativeThreadState: can drop the last reference to the old typeface, which releases a CoreText font.
// Native call path: SkFont::setTypeface -> ~sk_sp<SkTypeface> -> ~SkTypeface_Mac -> CFRelease
@ForceNativeThreadState
private external fun _nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSize")
// No ForceNativeThreadState: stores a new text size after clamping it to a valid range, so it is short and cannot
// block.
// Native call path: SkFont::setSize
private external fun _nSetSize(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetScaleX")
// No ForceNativeThreadState: stores a new horizontal scale value, so it is short and cannot block.
// Native call path: SkFont::setScaleX
private external fun _nSetScaleX(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nSetSkewX")
// No ForceNativeThreadState: stores a new skew value, so it is short and cannot block.
// Native call path: SkFont::setSkewX
private external fun _nSetSkewX(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetUTF32Glyph")
// Needs ForceNativeThreadState: looks up the glyph id for one character through CoreText, the Apple font library.
// Native call path: SkFont::unicharToGlyph -> SkTypeface_Mac::onCharsToGlyphs -> CTFontGetGlyphsForCharacters
@ForceNativeThreadState
private external fun _nGetUTF32Glyph(ptr: NativePointer, uni: Int): Short

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetUTF32Glyphs")
// Needs ForceNativeThreadState: looks up glyph ids for a list of characters through CoreText, so its time grows with
// the number of characters.
// Native call path: SkFont::unicharsToGlyphs -> SkTypeface_Mac::onCharsToGlyphs -> CTFontGetGlyphsForCharacters
@ForceNativeThreadState
private external fun _nGetUTF32Glyphs(ptr: NativePointer, uni: InteropPointer, uniArrLen: Int, resultGlyphs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetStringGlyphsCount")
// Needs ForceNativeThreadState: counts the glyphs in a string by scanning all of its text, so its time grows with the
// string length.
// Native call path: SkFont::countText -> SkFontPriv::CountTextElements
@ForceNativeThreadState
private external fun _nGetStringGlyphsCount(ptr: NativePointer, str: InteropPointer, len: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Font__1nMeasureText")
// Needs ForceNativeThreadState: maps text characters through CoreText and asks CoreText for glyph measurements missing
// from the cache.
// Native call path: SkFont::measureText -> SkAutoToGlyphs -> CTFontGetGlyphsForCharacters
@ForceNativeThreadState
private external fun _nMeasureText(ptr: NativePointer, str: InteropPointer, len: Int, paintPtr: NativePointer, rect: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nMeasureTextWidth")
// Needs ForceNativeThreadState: maps text characters through CoreText and asks CoreText for glyph measurements missing
// from the cache.
// Native call path: SkFont::measureText -> SkAutoToGlyphs -> CTFontGetGlyphsForCharacters
@ForceNativeThreadState
private external fun _nMeasureTextWidth(ptr: NativePointer, str: InteropPointer, len: Int, paintPtr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetWidths")
// Needs ForceNativeThreadState: reads glyph advance widths and, for a glyph not measured yet, asks CoreText for its
// metrics.
// Native call path: SkFont::getWidths -> SkBulkGlyphMetrics -> SkScalerContext_Mac::generateMetrics
@ForceNativeThreadState
private external fun _nGetWidths(ptr: NativePointer, glyphs: InteropPointer, count: Int, width: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetBounds")
// Needs ForceNativeThreadState: reads glyph bounding boxes and, for a glyph not measured yet, asks CoreText for its
// metrics.
// Native call path: SkFont::getBounds -> SkBulkGlyphMetrics -> SkScalerContext_Mac::generateMetrics
@ForceNativeThreadState
private external fun _nGetBounds(ptr: NativePointer, glyphs: InteropPointer, count: Int, paintPtr: NativePointer, bounds: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPositions")
// Needs ForceNativeThreadState: reads glyph positions and, for a glyph not measured yet, asks CoreText for its
// advances.
// Native call path: SkFont::getPos -> SkBulkGlyphMetrics -> SkScalerContext_Mac::generateMetrics
@ForceNativeThreadState
private external fun _nGetPositions(ptr: NativePointer, glyphs: InteropPointer, count: Int, x: Float, y: Float, positions: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetXPositions")
// Needs ForceNativeThreadState: reads glyph x positions and, for a glyph not measured yet, asks CoreText for its
// advances.
// Native call path: SkFont::getXPos -> SkBulkGlyphMetrics -> SkScalerContext_Mac::generateMetrics
@ForceNativeThreadState
private external fun _nGetXPositions(ptr: NativePointer, glyphs: InteropPointer, x: Float, count: Int, positions: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPath")
// Needs ForceNativeThreadState: builds the outline of a glyph, and if that outline is not built yet it asks CoreText to
// create it.
// Native call path: SkFont::getPath -> SkScalerContext_Mac::generatePath -> CTFontCreatePathForGlyph
@ForceNativeThreadState
private external fun _nGetPath(ptr: NativePointer, glyph: Short): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetPaths")
// Needs ForceNativeThreadState: builds glyph outlines for a list of glyphs, so its time grows with the number of
// glyphs, and asks CoreText for outlines not built yet.
// Native call path: SkFont::getPaths -> SkScalerContext_Mac::generatePath -> CTFontCreatePathForGlyph
@ForceNativeThreadState
private external fun _nGetPaths(ptr: NativePointer, glyphs: InteropPointer, count: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetMetrics")
// Needs ForceNativeThreadState: may build a new glyph cache for the font, which asks CoreText for the font's metrics.
// Native call path: SkFont::getMetrics -> SkScalerContext_Mac::generateFontMetrics
@ForceNativeThreadState
private external fun _nGetMetrics(ptr: NativePointer, metrics: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Font__1nGetSpacing")
// Needs ForceNativeThreadState: gets the line spacing from the font metrics, which may build a new glyph cache and asks
// CoreText for the metrics.
// Native call path: SkFont::getSpacing -> SkFont::getMetrics -> SkScalerContext_Mac::generateFontMetrics
@ForceNativeThreadState
private external fun _nGetSpacing(ptr: NativePointer): Float
