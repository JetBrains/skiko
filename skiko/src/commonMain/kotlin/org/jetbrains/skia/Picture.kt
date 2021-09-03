@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class Picture internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        /**
         * Recreates Picture that was serialized into data. Returns constructed Picture
         * if successful; otherwise, returns null. Fails if data does not permit
         * constructing valid Picture.
         */
        fun makeFromData(data: Data?): Picture? {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeFromData(getPtr(data))
                if (ptr == 0L) null else Picture(ptr)
            } finally {
                reachabilityBarrier(data)
            }
        }

        /**
         *
         * Returns a placeholder Picture. Result does not draw, and contains only
         * cull Rect, a hint of its bounds. Result is immutable; it cannot be changed
         * later. Result identifier is unique.
         *
         *
         * Returned placeholder can be intercepted during playback to insert other
         * commands into Canvas draw stream.
         *
         * @param cull  placeholder dimensions
         * @return      placeholder with unique identifier
         *
         * @see [https://fiddle.skia.org/c/@Picture_MakePlaceholder](https://fiddle.skia.org/c/@Picture_MakePlaceholder)
         */
        fun makePlaceholder(cull: Rect): Picture {
            Stats.onNativeCall()
            return Picture(_nMakePlaceholder(cull.left, cull.top, cull.right, cull.bottom))
        }

        @JvmStatic external fun _nMakeFromData(dataPtr: Long /*, SkDeserialProcs */): Long
        @JvmStatic external fun _nPlayback(ptr: Long, canvasPtr: Long, abort: BooleanSupplier?)
        @JvmStatic external fun _nGetCullRect(ptr: Long): Rect
        @JvmStatic external fun _nGetUniqueId(ptr: Long): Int
        @JvmStatic external fun _nSerializeToData(ptr: Long /*, SkSerialProcs */): Long
        @JvmStatic external fun _nMakePlaceholder(left: Float, top: Float, right: Float, bottom: Float): Long
        @JvmStatic external fun _nGetApproximateOpCount(ptr: Long): Int
        @JvmStatic external fun _nGetApproximateBytesUsed(ptr: Long): Long
        @JvmStatic external fun _nMakeShader(
            ptr: Long,
            tmx: Int,
            tmy: Int,
            filterMode: Int,
            localMatrix: FloatArray?,
            tileRect: Rect?
        ): Long

        init {
            staticLoad()
        }
    }
    /**
     *
     * Replays the drawing commands on the specified canvas. In the case that the
     * commands are recorded, each command in the Picture is sent separately to canvas.
     *
     *
     * To add a single command to draw Picture to recording canvas, call
     * [Canvas.drawPicture] instead.
     *
     * @param canvas  receiver of drawing commands
     * @param abort   return true to interrupt the playback
     * @return        this
     *
     * @see [https://fiddle.skia.org/c/@Picture_playback](https://fiddle.skia.org/c/@Picture_playback)
     */
    /**
     *
     * Replays the drawing commands on the specified canvas. In the case that the
     * commands are recorded, each command in the Picture is sent separately to canvas.
     *
     *
     * To add a single command to draw Picture to recording canvas, call
     * [Canvas.drawPicture] instead.
     *
     * @param canvas  receiver of drawing commands
     * @return        this
     *
     * @see [https://fiddle.skia.org/c/@Picture_playback](https://fiddle.skia.org/c/@Picture_playback)
     */
    fun playback(canvas: Canvas?, abort: BooleanSupplier? = null): Picture {
        return try {
            Stats.onNativeCall()
            _nPlayback(_ptr, getPtr(canvas), abort)
            this
        } finally {
            reachabilityBarrier(canvas)
        }
    }

    /**
     *
     * Returns cull Rect for this picture, passed in when Picture was created.
     * Returned Rect does not specify clipping Rect for Picture; cull is hint
     * of Picture bounds.
     *
     *
     * Picture is free to discard recorded drawing commands that fall outside cull.
     *
     * @return  bounds passed when Picture was created
     *
     * @see [https://fiddle.skia.org/c/@Picture_cullRect](https://fiddle.skia.org/c/@Picture_cullRect)
     */
    val cullRect: Rect
        get() = try {
            Stats.onNativeCall()
            _nGetCullRect(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns a non-zero value unique among Picture in Skia process.
     *
     * @return  identifier for Picture
     */
    val uniqueId: Int
        get() = try {
            Stats.onNativeCall()
            _nGetUniqueId(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  storage containing Data describing Picture.
     *
     * @see [https://fiddle.skia.org/c/@Picture_serialize](https://fiddle.skia.org/c/@Picture_serialize)
     */
    fun serializeToData(): Data {
        return try {
            Stats.onNativeCall()
            Data(_nSerializeToData(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns the approximate number of operations in SkPicture. Returned value
     * may be greater or less than the number of SkCanvas calls
     * recorded: some calls may be recorded as more than one operation, other
     * calls may be optimized away.
     *
     * @return  approximate operation count
     *
     * @see [https://fiddle.skia.org/c/@Picture_approximateOpCount](https://fiddle.skia.org/c/@Picture_approximateOpCount)
     */
    val approximateOpCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetApproximateOpCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns the approximate byte size of Picture. Does not include large objects
     * referenced by Picture.
     *
     * @return  approximate size
     *
     * @see [https://fiddle.skia.org/c/@Picture_approximateBytesUsed](https://fiddle.skia.org/c/@Picture_approximateBytesUsed)
     */
    val approximateBytesUsed: Long
        get() = try {
            Stats.onNativeCall()
            _nGetApproximateBytesUsed(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    /**
     * Return a new shader that will draw with this picture.
     *
     * @param tmx          The tiling mode to use when sampling in the x-direction.
     * @param tmy          The tiling mode to use when sampling in the y-direction.
     * @param mode         How to filter the tiles
     * @param localMatrix  Optional matrix used when sampling
     * @param tileRect     The tile rectangle in picture coordinates: this represents the subset
     * (or superset) of the picture used when building a tile. It is not
     * affected by localMatrix and does not imply scaling (only translation
     * and cropping). If null, the tile rect is considered equal to the picture
     * bounds.
     * @return             Returns a new shader object. Note: this function never returns null.
     */
    /**
     * Return a new shader that will draw with this picture. The tile rect is considered
     * equal to the picture bounds.
     *
     * @param tmx   The tiling mode to use when sampling in the x-direction.
     * @param tmy   The tiling mode to use when sampling in the y-direction.
     * @param mode  How to filter the tiles
     * @return      Returns a new shader object. Note: this function never returns null.
     */
    /**
     * Return a new shader that will draw with this picture. The tile rect is considered
     * equal to the picture bounds.
     *
     * @param tmx          The tiling mode to use when sampling in the x-direction.
     * @param tmy          The tiling mode to use when sampling in the y-direction.
     * @param mode         How to filter the tiles
     * @param localMatrix  Optional matrix used when sampling
     * @return             Returns a new shader object. Note: this function never returns null.
     */
    fun makeShader(
        tmx: FilterTileMode,
        tmy: FilterTileMode,
        mode: FilterMode,
        localMatrix: Matrix33? = null,
        tileRect: Rect? = null
    ): Shader {
        return try {
            Stats.onNativeCall()
            val arr = localMatrix?.mat
            Shader(_nMakeShader(_ptr, tmx.ordinal, tmy.ordinal, mode.ordinal, arr, tileRect))
        } finally {
            reachabilityBarrier(this)
        }
    }
}