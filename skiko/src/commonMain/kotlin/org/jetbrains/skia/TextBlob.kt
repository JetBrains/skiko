package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withNullableResult
import org.jetbrains.skia.impl.withResult

class TextBlob internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR), Iterable<TextBlob.TextBlobIter.Run> {
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
                val ptr = interopScope {
                    TextBlob_nMakeFromPosH(
                        toInterop(glyphs),
                        glyphs.size,
                        toInterop(xpos),
                        ypos,
                        getPtr(font)
                    )
                }
                if (ptr == NullPointer) null else TextBlob(ptr)
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
                    interopScope {
                        TextBlob_nMakeFromPos(toInterop(glyphs), glyphs.size, toInterop(floatPos), getPtr(font))
                    }
                if (ptr == NullPointer) null else TextBlob(ptr)
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
                val ptr = interopScope {
                    TextBlob_nMakeFromRSXform(
                        toInterop(glyphs),
                        glyphs.size,
                        toInterop(floatXform),
                        getPtr(font)
                    )
                }
                if (ptr == NullPointer) null else TextBlob(ptr)
            } finally {
                reachabilityBarrier(font)
            }
        }

        fun makeFromData(data: Data?): TextBlob? {
            return try {
                Stats.onNativeCall()
                val ptr = TextBlob_nMakeFromData(getPtr(data))
                if (ptr == NullPointer) null else TextBlob(ptr)
            } finally {
                reachabilityBarrier(data)
            }
        }

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
            Rect.fromInteropPointer {
                TextBlob_nBounds(_ptr, it)
            }
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
            TextBlob_nGetUniqueId(_ptr)
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
        return getIntercepts(lowerBound, upperBound, null)
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
            withResult(FloatArray(TextBlob_nGetInterceptsLength(_ptr, lowerBound, upperBound, getPtr(paint)))) {
                TextBlob_nGetIntercepts(
                    ptr = _ptr,
                    lower = lowerBound,
                    upper = upperBound,
                    paintPtr = getPtr(paint),
                    resultArray = it
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
    }

    fun serializeToData(): Data {
        return try {
            Stats.onNativeCall()
            Data(TextBlob_nSerializeToData(_ptr))
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
            withResult(ShortArray(glyphsLength)) {
                TextBlob_nGetGlyphs(_ptr, it)
            }
        } finally {
            reachabilityBarrier(this)
        }

    internal val glyphsLength: Int
        get() = try {
            Stats.onNativeCall()
            TextBlob_nGetGlyphsLength(_ptr)
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
            withResult(FloatArray(TextBlob_nGetPositionsLength(_ptr))) {
                TextBlob_nGetPositions(_ptr, it)
            }
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
            withNullableResult(IntArray(TextBlob_nGetClustersLength(_ptr))) {
                TextBlob_nGetClusters(_ptr, it)
            } ?: throw IllegalArgumentException()
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
            Rect.fromInteropPointerNullable {
                TextBlob_nGetTightBounds(_ptr, it)
            } ?: throw IllegalArgumentException()
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
            Rect.fromInteropPointerNullable {
                TextBlob_nGetBlockBounds(_ptr, it)
            } ?: throw IllegalArgumentException()
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
            withNullableResult(FloatArray(1)) {
                TextBlob_nGetFirstBaseline(_ptr, it)
            }?.firstOrNull() ?: throw IllegalArgumentException()
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
            withNullableResult(FloatArray(1)) {
                TextBlob_nGetLastBaseline(_ptr, it)
            }?.firstOrNull() ?: throw IllegalArgumentException()
        } finally {
            reachabilityBarrier(this)
        }

    private object _FinalizerHolder {
        val PTR = TextBlob_nGetFinalizer()
    }

    override fun iterator(): Iterator<TextBlobIter.Run> = TextBlobIter(this)

    class TextBlobIter(val textBlob: TextBlob) : Iterator<TextBlobIter.Run>, Managed(Iter_nCreate(textBlob._ptr), FinalizerHolder.PTR) {
        data class Run(val typeface: Typeface, val glyphs: ShortArray) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Run) return false

                return typeface == other.typeface && glyphs.contentEquals(other.glyphs)
            }

            override fun hashCode(): Int {
                var result = typeface.hashCode()
                result = 31 * result + glyphs.contentHashCode()
                return result
            }
        }

        override fun hasNext(): Boolean {
            Stats.onNativeCall()
            try {
                return Iter_nHasNext(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

        override fun next(): Run {
            Stats.onNativeCall()
            try {
                val typeface = Typeface(Iter_nGetTypeface(_ptr))
                val glyphCount = Iter_nGetGlyphCount(_ptr)
                val glyphs = withResult(ShortArray(glyphCount)) {
                    Iter_nGetGlyphs(_ptr, it, glyphCount)
                }
                Iter_nFetch(_ptr)
                return Run(typeface, glyphs)
            } finally {
                reachabilityBarrier(this)
            }
        }

        private object FinalizerHolder {
            val PTR = Iter_nGetFinalizer()
        }
    }
}