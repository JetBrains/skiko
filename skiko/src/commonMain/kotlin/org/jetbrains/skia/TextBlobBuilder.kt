@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import kotlin.jvm.JvmStatic

class TextBlobBuilder internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nGetFinalizer")
        external fun _nGetFinalizer(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nMake")
        external fun _nMake(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nBuild")
        external fun _nBuild(ptr: Long): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRun")
        external fun _nAppendRun(ptr: Long, fontPtr: Long, glyphs: ShortArray?, x: Float, y: Float, bounds: Rect?)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRunPosH")
        external fun _nAppendRunPosH(
            ptr: Long,
            fontPtr: Long,
            glyphs: ShortArray?,
            xs: FloatArray?,
            y: Float,
            bounds: Rect?
        )

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRunPos")
        external fun _nAppendRunPos(ptr: Long, fontPtr: Long, glyphs: ShortArray?, pos: FloatArray?, bounds: Rect?)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_TextBlobBuilder__1nAppendRunRSXform")
        external fun _nAppendRunRSXform(ptr: Long, fontPtr: Long, glyphs: ShortArray?, xform: FloatArray?)

        init {
            staticLoad()
        }
    }

    /**
     * Constructs empty TextBlobBuilder. By default, TextBlobBuilder has no runs.
     *
     * @see [https://fiddle.skia.org/c/@TextBlobBuilder_empty_constructor](https://fiddle.skia.org/c/@TextBlobBuilder_empty_constructor)
     */
    constructor() : this(_nMake()) {
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
            if (ptr == 0L) null else TextBlob(ptr)
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
     * @return        this
     */
    fun appendRun(font: Font, text: String, x: Float, y: Float): TextBlobBuilder {
        return appendRun(font, font.getStringGlyphs(text), x, y, null)
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
     * @param text    Text to append in this run
     * @param x       horizontal offset within the blob
     * @param y       vertical offset within the blob
     * @param bounds  optional run bounding box
     * @return        this
     */
    fun appendRun(font: Font, text: String, x: Float, y: Float, bounds: Rect?): TextBlobBuilder {
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
            _nAppendRun(
                _ptr,
                getPtr(font),
                glyphs,
                x,
                y,
                bounds
            )
            this
        } finally {
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
            _nAppendRunPosH(
                _ptr,
                getPtr(font),
                glyphs,
                xs,
                y,
                bounds
            )
            this
        } finally {
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
            _nAppendRunPos(
                _ptr,
                getPtr(font),
                glyphs,
                floatPos,
                bounds
            )
            this
        } finally {
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
            _nAppendRunRSXform(
                _ptr,
                getPtr(font),
                glyphs,
                floatXform
            )
            this
        } finally {
            reachabilityBarrier(font)
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}