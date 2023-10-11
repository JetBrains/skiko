package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class Picture internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        /**
         * Recreates Picture that was serialized into data. Returns constructed Picture
         * if successful; otherwise, returns null. Fails if data does not permit
         * constructing valid Picture.
         */
        fun makeFromData(data: Data?): Picture? {
            return try {
                Stats.onNativeCall()
                val ptr = Picture_nMakeFromData(getPtr(data))
                if (ptr == NullPointer) null else Picture(ptr)
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
    fun playback(canvas: Canvas?, abort: (() -> Boolean)? = null): Picture {
        return try {
            Stats.onNativeCall()
            interopScope {
                _nPlayback(_ptr, getPtr(canvas), booleanCallback(abort))
            }
            this
        } finally {
            reachabilityBarrier(this)
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
            Rect.fromInteropPointer { _nGetCullRect(_ptr, it) }
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
    val approximateBytesUsed: NativePointer
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
            Shader(
                interopScope {
                    _nMakeShader(
                        _ptr,
                        tmx.ordinal,
                        tmy.ordinal,
                        mode.ordinal,
                        toInterop(arr),
                        tileRect != null,
                        tileRect?.left ?: 0f,
                        tileRect?.top ?: 0f,
                        tileRect?.right ?: 0f,
                        tileRect?.bottom ?: 0f
                    )
                }
            )
        } finally {
            reachabilityBarrier(this)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_Picture__1nMakeFromData")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Picture__1nMakeFromData")
private external fun Picture_nMakeFromData(dataPtr: NativePointer /*, SkDeserialProcs */): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nGetCullRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Picture__1nGetCullRect")
private external fun _nGetCullRect(ptr: NativePointer, ltrb: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Picture__1nGetUniqueId")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Picture__1nGetUniqueId")
private external fun _nGetUniqueId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Picture__1nSerializeToData")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Picture__1nSerializeToData")
private external fun _nSerializeToData(ptr: NativePointer /*, SkSerialProcs */): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nMakePlaceholder")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Picture__1nMakePlaceholder")
private external fun _nMakePlaceholder(left: Float, top: Float, right: Float, bottom: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nGetApproximateOpCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Picture__1nGetApproximateOpCount")
private external fun _nGetApproximateOpCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Picture__1nGetApproximateBytesUsed")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Picture__1nGetApproximateBytesUsed")
private external fun _nGetApproximateBytesUsed(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nMakeShader")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Picture__1nMakeShader")
private external fun _nMakeShader(
    ptr: NativePointer,
    tmx: Int,
    tmy: Int,
    filterMode: Int,
    localMatrix: InteropPointer,
    hasTile: Boolean,
    tileL: Float,
    tileT: Float,
    tileR: Float,
    tileB: Float,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Picture__1nPlayback")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Picture__1nPlayback")
private external fun _nPlayback(ptr: NativePointer, canvasPtr: NativePointer, data: InteropPointer)