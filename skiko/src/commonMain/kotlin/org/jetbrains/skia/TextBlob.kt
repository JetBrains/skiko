@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class TextBlob internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        /**
         * Returns a TextBlob built from a single run of text with x-positions and a single y value.
         * Returns null if glyphs is empty.
         *
         * @param glyphs  glyphs drawn
         * @param xpos    array of x-positions, must contain values for all of the glyphs.
         * @param ypos    shared y-position for each glyph, to be paired with each xpos.
         * @param font    Font used for this run
         * @return        new TextBlob or null
         */
        fun makeFromPosH(glyphs: ShortArray, xpos: FloatArray, ypos: Float, font: Font?): TextBlob? {
            return try {
                require(glyphs.size == xpos.size) { "glyphs.length " + glyphs.size + " != xpos.length " + xpos.size }
                Stats.onNativeCall()
                val ptr = _nMakeFromPosH(
                    glyphs,
                    xpos,
                    ypos,
                    getPtr(font)
                )
                if (ptr == 0L) null else TextBlob(ptr)
            } finally {
                reachabilityBarrier(font)
            }
        }

        /**
         * Returns a TextBlob built from a single run of text with positions.
         * Returns null if glyphs is empty.
         *
         * @param glyphs  glyphs drawn
         * @param pos     array of positions, must contain values for all of the glyphs.
         * @param font    Font used for this run
         * @return        new TextBlob or null
         */
        fun makeFromPos(glyphs: ShortArray, pos: Array<Point>, font: Font?): TextBlob? {
            return try {
                require(glyphs.size == pos.size) { "glyphs.length " + glyphs.size + " != pos.length " + pos.size }
                val floatPos = FloatArray(pos.size * 2)
                for (i in pos.indices) {
                    floatPos[i * 2] = pos[i].x
                    floatPos[i * 2 + 1] = pos[i].y
                }
                Stats.onNativeCall()
                val ptr =
                    _nMakeFromPos(glyphs, floatPos, getPtr(font))
                if (ptr == 0L) null else TextBlob(ptr)
            } finally {
                reachabilityBarrier(font)
            }
        }

        fun makeFromRSXform(glyphs: ShortArray, xform: Array<RSXform>, font: Font?): TextBlob? {
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
                val ptr = _nMakeFromRSXform(
                    glyphs,
                    floatXform,
                    getPtr(font)
                )
                if (ptr == 0L) null else TextBlob(ptr)
            } finally {
                reachabilityBarrier(font)
            }
        }

        fun makeFromData(data: Data?): TextBlob? {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeFromData(getPtr(data))
                if (ptr == 0L) null else TextBlob(ptr)
            } finally {
                reachabilityBarrier(data)
            }
        }

        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nBounds(ptr: Long): Rect
        @JvmStatic external fun _nGetUniqueId(ptr: Long): Int
        @JvmStatic external fun _nGetIntercepts(ptr: Long, lower: Float, upper: Float, paintPtr: Long): FloatArray?
        @JvmStatic external fun _nMakeFromPosH(glyphs: ShortArray?, xpos: FloatArray?, ypos: Float, fontPtr: Long): Long
        @JvmStatic external fun _nMakeFromPos(glyphs: ShortArray?, pos: FloatArray?, fontPtr: Long): Long
        @JvmStatic external fun _nMakeFromRSXform(glyphs: ShortArray?, xform: FloatArray?, fontPtr: Long): Long
        @JvmStatic external fun _nSerializeToData(ptr: Long /*, SkSerialProcs */): Long
        @JvmStatic external fun _nMakeFromData(dataPtr: Long /*, SkDeserialProcs */): Long
        @JvmStatic external fun _nGetGlyphs(ptr: Long): ShortArray
        @JvmStatic external fun _nGetPositions(ptr: Long): FloatArray
        @JvmStatic external fun _nGetClusters(ptr: Long): IntArray
        @JvmStatic external fun _nGetTightBounds(ptr: Long): Rect
        @JvmStatic external fun _nGetBlockBounds(ptr: Long): Rect
        @JvmStatic external fun _nGetFirstBaseline(ptr: Long): Float
        @JvmStatic external fun _nGetLastBaseline(ptr: Long): Float

        init {
            staticLoad()
        }
    }

    /**
     * Returns conservative bounding box. Uses Paint associated with each glyph to
     * determine glyph bounds, and unions all bounds. Returned bounds may be
     * larger than the bounds of all glyphs in runs.
     *
     * @return  conservative bounding box
     */
    val bounds: Rect
        get() = try {
            Stats.onNativeCall()
            _nBounds(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns a non-zero value unique among all text blobs.
     *
     * @return  identifier for TextBlob
     */
    val uniqueId: Int
        get() = try {
            Stats.onNativeCall()
            _nGetUniqueId(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Returns the number of intervals that intersect bounds.
     * bounds describes a pair of lines parallel to the text advance.
     * The return array size is a multiple of two, and is at most twice the number of glyphs in
     * the the blob.
     *
     *
     * Runs within the blob that contain SkRSXform are ignored when computing intercepts.
     *
     * @param lowerBound lower line parallel to the advance
     * @param upperBound upper line parallel to the advance
     * @return           intersections; may be null
     */
    fun getIntercepts(lowerBound: Float, upperBound: Float): FloatArray? {
        return getIntercepts(lowerBound, upperBound)
    }

    /**
     *
     * Returns the number of intervals that intersect bounds.
     * bounds describes a pair of lines parallel to the text advance.
     * The return array size is a multiple of two, and is at most twice the number of glyphs in
     * the the blob.
     *
     *
     * Runs within the blob that contain SkRSXform are ignored when computing intercepts.
     *
     * @param lowerBound lower line parallel to the advance
     * @param upperBound upper line parallel to the advance
     * @param paint      specifies stroking, PathEffect that affects the result; may be null
     * @return           intersections; may be null
     */
    fun getIntercepts(lowerBound: Float, upperBound: Float, paint: Paint?): FloatArray? {
        return try {
            Stats.onNativeCall()
            _nGetIntercepts(
                _ptr,
                lowerBound,
                upperBound,
                getPtr(paint)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
    }

    fun serializeToData(): Data {
        return try {
            Stats.onNativeCall()
            Data(_nSerializeToData(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * @return  glyph indices for the whole blob
     */
    val glyphs: ShortArray
        get() = try {
            Stats.onNativeCall()
            _nGetGlyphs(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Return result depends on how blob was constructed.
     *
     *  * makeFromPosH returns 1 float per glyph (x pos)
     *  * makeFromPos returns 2 floats per glyph (x, y pos)
     *  * makeFromRSXform returns 4 floats per glyph
     *
     *
     *
     * Blobs constructed by TextBlobBuilderRunHandler/Shaper default handler have 2 floats per glyph.
     *
     * @return  glyph positions for the blob if it was made with makeFromPos, null otherwise
     */
    val positions: FloatArray
        get() = try {
            Stats.onNativeCall()
            _nGetPositions(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Only works on TextBlobs that come from TextBlobBuilderRunHandler/Shaper default handler.
     *
     * @return  utf-16 offsets of clusters that start the glyph
     * @throws  IllegalArgumentException if TextBlob doesn’t have this information
     */
    val clusters: IntArray
        get() = try {
            Stats.onNativeCall()
            val res = _nGetClusters(_ptr) ?: throw IllegalArgumentException()
            res
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Only works on TextBlobs that come from TextBlobBuilderRunHandler/Shaper default handler.
     *
     * @return  tight bounds around all the glyphs in the TextBlob
     * @throws  IllegalArgumentException if TextBlob doesn’t have this information
     */
    val tightBounds: Rect
        get() = try {
            Stats.onNativeCall()
            val res = _nGetTightBounds(_ptr) ?: throw IllegalArgumentException()
            res
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Only works on TextBlobs that come from TextBlobBuilderRunHandler/Shaper default handler.
     *
     * @return  tight bounds around all the glyphs in the TextBlob
     * @throws  IllegalArgumentException if TextBlob doesn’t have this information
     */
    val blockBounds: Rect
        get() = try {
            Stats.onNativeCall()
            val res = _nGetBlockBounds(_ptr) ?: throw IllegalArgumentException()
            res
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Only works on TextBlobs that come from TextBlobBuilderRunHandler/Shaper default handler.
     *
     * @return  first baseline in TextBlob
     * @throws  IllegalArgumentException if TextBlob doesn’t have this information
     */
    val firstBaseline: Float
        get() = try {
            Stats.onNativeCall()
            val res = _nGetFirstBaseline(_ptr) ?: throw IllegalArgumentException()
            res
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Only works on TextBlobs that come from TextBlobBuilderRunHandler/Shaper default handler.
     *
     * @return  last baseline in TextBlob
     * @throws  IllegalArgumentException if TextBlob doesn’t have this information
     */
    val lastBaseline: Float
        get() = try {
            Stats.onNativeCall()
            val res = _nGetLastBaseline(_ptr) ?: throw IllegalArgumentException()
            res
        } finally {
            reachabilityBarrier(this)
        }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}