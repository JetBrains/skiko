package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class TextBlobBuilder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    /**
     * Constructs empty TextBlobBuilder. By default, TextBlobBuilder has no runs.
     *
     * @see [https://fiddle.skia.org/c/@TextBlobBuilder_empty_constructor](https://fiddle.skia.org/c/@TextBlobBuilder_empty_constructor)
     */
    constructor() : this(TextBlobBuilder_nMake()) {
        Stats.onNativeCall()
    }

    /**
     *
     * Returns TextBlob built from runs of glyphs added by builder. Returned
     * TextBlob is immutable; it may be copied, but its contents may not be altered.
     * Returns null if no runs of glyphs were added by builder.
     *
     *
     * Resets TextBlobBuilder to its initial empty state, allowing it to be
     * reused to build a new set of runs.
     *
     * @return  SkTextBlob or null
     *
     * @see [https://fiddle.skia.org/c/@TextBlobBuilder_make](https://fiddle.skia.org/c/@TextBlobBuilder_make)
     */
    fun build(): TextBlob? {
        return try {
            Stats.onNativeCall()
            val ptr = _nBuild(_ptr)
            if (ptr == NullPointer) null else TextBlob(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Glyphs are positioned on a baseline at (x, y), using font metrics to
     * determine their relative placement.
     *
     * @param font    Font used for this run
     * @param text    Text to append in this run
     * @param x       horizontal offset within the blob
     * @param y       vertical offset within the blob
     * @param bounds  optional run bounding box
     * @return        this
     */
   fun appendRun(font: Font, text: String, x: Float, y: Float, bounds: Rect? = null): TextBlobBuilder {
        return appendRun(font, font.getStringGlyphs(text), x, y, bounds)
    }

    /**
     *
     * Glyphs are positioned on a baseline at (x, y), using font metrics to
     * determine their relative placement.
     *
     *
     * bounds defines an optional bounding box, used to suppress drawing when TextBlob
     * bounds does not intersect Surface bounds. If bounds is null, TextBlob bounds
     * is computed from (x, y) and glyphs metrics.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param x       horizontal offset within the blob
     * @param y       vertical offset within the blob
     * @param bounds  optional run bounding box
     * @return        this
     */
    fun appendRun(font: Font?, glyphs: ShortArray?, x: Float, y: Float, bounds: Rect?): TextBlobBuilder {
        return try {
            Stats.onNativeCall()
            interopScope {
                _nAppendRun(
                    _ptr,
                    getPtr(font),
                    toInterop(glyphs),
                    glyphs?.size ?: 0,
                    x,
                    y,
                    toInterop(bounds?.serializeToFloatArray())
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(font)
        }
    }

    /**
     *
     * Glyphs are positioned on a baseline at y, using x-axis positions from xs.
     *
     *
     * bounds defines an optional bounding box, used to suppress drawing when TextBlob
     * bounds does not intersect Surface bounds. If bounds is null, TextBlob bounds
     * is computed from (x, y) and glyphs metrics.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param xs      horizontal positions of glyphs within the blob
     * @param y       vertical offset within the blob
     * @param bounds  optional run bounding box
     * @return        this
     */
    /**
     *
     * Glyphs are positioned on a baseline at y, using x-axis positions from xs.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param xs      horizontal positions on glyphs within the blob
     * @param y       vertical offset within the blob
     * @return        this
     */
    fun appendRunPosH(
        font: Font?,
        glyphs: ShortArray,
        xs: FloatArray,
        y: Float,
        bounds: Rect? = null
    ): TextBlobBuilder {
        return try {
            require(glyphs.size == xs.size) { "glyphs.length " + glyphs.size + " != xs.length " + xs.size }
            Stats.onNativeCall()
            interopScope {
                _nAppendRunPosH(
                    _ptr,
                    getPtr(font),
                    toInterop(glyphs),
                    glyphs.size,
                    toInterop(xs),
                    y,
                    toInterop(bounds?.serializeToFloatArray())
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(font)
        }
    }

    /**
     *
     * Glyphs are positioned at positions from pos.
     *
     *
     * bounds defines an optional bounding box, used to suppress drawing when TextBlob
     * bounds does not intersect Surface bounds. If bounds is null, TextBlob bounds
     * is computed from (x, y) and glyphs metrics.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param pos     positions of glyphs within the blob
     * @param bounds  optional run bounding box
     * @return        this
     */
    /**
     *
     * Glyphs are positioned at positions from pos.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param pos     positions of glyphs within the blob
     * @return        this
     */
   fun appendRunPos(font: Font?, glyphs: ShortArray, pos: Array<Point>, bounds: Rect? = null): TextBlobBuilder {
        return try {
            require(glyphs.size == pos.size) { "glyphs.length " + glyphs.size + " != pos.length " + pos.size }
            val floatPos = FloatArray(pos.size * 2)
            for (i in pos.indices) {
                floatPos[i * 2] = pos[i].x
                floatPos[i * 2 + 1] = pos[i].y
            }
            Stats.onNativeCall()
            interopScope {
                _nAppendRunPos(
                    _ptr,
                    getPtr(font),
                    toInterop(glyphs),
                    glyphs.size,
                    toInterop(floatPos),
                    toInterop(bounds?.serializeToFloatArray())
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(font)
        }
    }

    fun appendRunRSXform(font: Font?, glyphs: ShortArray, xform: Array<RSXform>): TextBlobBuilder {
        return try {
            require(glyphs.size == xform.size) { "glyphs.length " + glyphs.size + " != xform.length " + xform.size }
            val floatXform = FloatArray(xform.size * 4)
            for (i in xform.indices) {
                floatXform[i * 4] = xform[i].scos
                floatXform[i * 4 + 1] = xform[i].ssin
                floatXform[i * 4 + 2] = xform[i].tx
                floatXform[i * 4 + 3] = xform[i].ty
            }
            Stats.onNativeCall()
            interopScope {
                _nAppendRunRSXform(
                    _ptr,
                    getPtr(font),
                    toInterop(glyphs),
                    glyphs.size,
                    toInterop(floatXform)
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(font)
        }
    }

    private object _FinalizerHolder {
        val PTR = TextBlobBuilder_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of a cleanup function without calling it, so it is short and cannot
// block.
// Native call path: deleteTextBlobBuilder
private external fun TextBlobBuilder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nMake")
// No ForceNativeThreadState: creates an empty builder and zeroes its fields, owning no font or typeface yet, so it is
// short and cannot block.
// Native call path: SkTextBlobBuilder::SkTextBlobBuilder
private external fun TextBlobBuilder_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nBuild")
// Needs ForceNativeThreadState: finishes the text blob and may measure glyph bounds through CoreText, so it can call
// CoreText.
// Native call path: SkTextBlobBuilder::make -> updateDeferredBounds -> TightRunBounds -> SkFont::measureText
@ForceNativeThreadState
private external fun _nBuild(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRun")
// Needs ForceNativeThreadState: adds a text run and can first finish the previous run's bounds through CoreText, so it
// can call CoreText.
// Native call path: allocRun -> allocInternal -> updateDeferredBounds -> SkFont::measureText
@ForceNativeThreadState
private external fun _nAppendRun(
    ptr: NativePointer, fontPtr: NativePointer,
    glyphs: InteropPointer, glyphsLen: Int,
    x: Float, y: Float,
    bounds: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH")
// Needs ForceNativeThreadState: adds a text run and can first finish the previous run's bounds through CoreText, so it
// can call CoreText.
// Native call path: allocRunPosH -> allocInternal -> updateDeferredBounds -> GetFontBounds
@ForceNativeThreadState
private external fun _nAppendRunPosH(
    ptr: NativePointer,
    fontPtr: NativePointer,
    glyphs: InteropPointer,
    glyphsLen: Int,
    xs: InteropPointer,
    y: Float,
    bounds: InteropPointer
)


@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos")
// Needs ForceNativeThreadState: adds a text run and can first finish the previous run's bounds through CoreText, so it
// can call CoreText.
// Native call path: allocRunPos -> allocInternal -> updateDeferredBounds -> GetFontBounds
@ForceNativeThreadState
private external fun _nAppendRunPos(
    ptr: NativePointer, fontPtr: NativePointer,
    glyphs: InteropPointer, glyphsLen: Int,
    pos: InteropPointer,
    bounds: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRunRSXform")
// Needs ForceNativeThreadState: adds a text run and can first finish the previous run's bounds through CoreText, so it
// can call CoreText.
// Native call path: allocRunRSXform -> allocInternal -> updateDeferredBounds -> GetFontBounds
@ForceNativeThreadState
private external fun _nAppendRunRSXform(
    ptr: NativePointer, fontPtr: NativePointer,
    glyphs: InteropPointer, glyphsLen: Int,
    xform: InteropPointer
)
