package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skiko.InternalSkikoApi

class Surface : RefCnt {
    companion object {
        fun makeRasterDirect(pixmap: Pixmap): Surface {
            return makeRasterDirect(pixmap, null)
        }

        /**
         *
         * Allocates raster Surface. Canvas returned by Surface draws directly into pixels.
         *
         *
         * Surface is returned if all parameters are valid. Valid parameters include:
         *
         *  * info dimensions are greater than zero;
         *  * info contains ColorType and AlphaType supported by raster surface;
         *  * pixelsPtr is not 0;
         *  * rowBytes is large enough to contain info width pixels of ColorType.
         *
         *
         * Pixel buffer size should be info height times computed rowBytes.
         *
         *
         * Pixels are not initialized.
         *
         *
         * To access pixels after drawing, peekPixels() or readPixels().
         *
         * @param imageInfo     width, height, ColorType, AlphaType, ColorSpace,
         * of raster surface; width and height must be greater than zero
         * @param pixelsPtr     pointer to destination pixels buffer
         * @param rowBytes      memory address of destination native pixels buffer
         * @return              created Surface
         */
        fun makeRasterDirect(
            imageInfo: ImageInfo,
            pixelsPtr: NativePointer,
            rowBytes: Int
        ): Surface {
            return makeRasterDirect(imageInfo, pixelsPtr, rowBytes, null)
        }

        fun makeRasterDirect(
            pixmap: Pixmap,
            surfaceProps: SurfaceProps?
        ): Surface {
            return try {
                Stats.onNativeCall()
                val ptr = interopScope {
                    _nMakeRasterDirectWithPixmap(
                        getPtr(pixmap), toInterop(surfaceProps?.packToIntArray())
                    )
                }
                require(ptr != NullPointer) {
                    "Failed Surface.makeRasterDirect($pixmap, $surfaceProps)"
                }
                Surface(ptr)
            } finally {
                reachabilityBarrier(pixmap)
            }
        }

        /**
         *
         * Allocates raster Surface. Canvas returned by Surface draws directly into pixels.
         *
         *
         * Surface is returned if all parameters are valid. Valid parameters include:
         *
         *  * info dimensions are greater than zero;
         *  * info contains ColorType and AlphaType supported by raster surface;
         *  * pixelsPtr is not 0;
         *  * rowBytes is large enough to contain info width pixels of ColorType.
         *
         *
         * Pixel buffer size should be info height times computed rowBytes.
         *
         *
         * Pixels are not initialized.
         *
         *
         * To access pixels after drawing, peekPixels() or readPixels().
         *
         * @param imageInfo     width, height, ColorType, AlphaType, ColorSpace,
         * of raster surface; width and height must be greater than zero
         * @param pixelsPtr     pointer to destination pixels buffer
         * @param rowBytes      memory address of destination native pixels buffer
         * @param surfaceProps  LCD striping orientation and setting for device independent fonts;
         * may be null
         * @return              created Surface
         */
        fun makeRasterDirect(
            imageInfo: ImageInfo,
            pixelsPtr: NativePointer,
            rowBytes: Int,
            surfaceProps: SurfaceProps?
        ): Surface {
            return try {
                Stats.onNativeCall()
                val ptr = interopScope {
                    _nMakeRasterDirect(
                        imageInfo.width,
                        imageInfo.height,
                        imageInfo.colorInfo.colorType.ordinal,
                        imageInfo.colorInfo.alphaType.ordinal,
                        getPtr(imageInfo.colorInfo.colorSpace),
                        pixelsPtr,
                        rowBytes,
                        toInterop(surfaceProps?.packToIntArray())
                    )
                }
                require(ptr != NullPointer) {
                    "Failed Surface.makeRasterDirect($imageInfo, $pixelsPtr, $rowBytes, $surfaceProps)"
                }
                Surface(ptr)
            } finally {
                reachabilityBarrier(imageInfo.colorInfo.colorSpace)
            }
        }

        /**
         *
         * Allocates raster Surface. Canvas returned by Surface draws directly into pixels.
         * Allocates and zeroes pixel memory. Pixel memory size is imageInfo.height() times imageInfo.minRowBytes().
         * Pixel memory is deleted when Surface is deleted.
         *
         *
         * Surface is returned if all parameters are valid. Valid parameters include:
         *
         *  * info dimensions are greater than zero;
         *  * info contains ColorType and AlphaType supported by raster surface;
         *
         * @param imageInfo     width, height, ColorType, AlphaType, ColorSpace,
         * of raster surface; width and height must be greater than zero
         * @return              new Surface
         */
        fun makeRaster(imageInfo: ImageInfo): Surface {
            return makeRaster(imageInfo, imageInfo.minRowBytes, null)
        }

        /**
         *
         * Allocates raster Surface. Canvas returned by Surface draws directly into pixels.
         * Allocates and zeroes pixel memory. Pixel memory size is imageInfo.height() times
         * rowBytes, or times imageInfo.minRowBytes() if rowBytes is zero.
         * Pixel memory is deleted when Surface is deleted.
         *
         *
         * Surface is returned if all parameters are valid. Valid parameters include:
         *
         *  * info dimensions are greater than zero;
         *  * info contains ColorType and AlphaType supported by raster surface;
         *  * rowBytes is large enough to contain info width pixels of ColorType, or is zero.
         *
         *
         * If rowBytes is zero, a suitable value will be chosen internally.
         *
         * @param imageInfo     width, height, ColorType, AlphaType, ColorSpace,
         * of raster surface; width and height must be greater than zero
         * @param rowBytes      interval from one Surface row to the next; may be zero
         * @return              new Surface
         */
        fun makeRaster(
            imageInfo: ImageInfo,
            rowBytes: Int
        ): Surface {
            return makeRaster(imageInfo, rowBytes, null)
        }

        /**
         *
         * Allocates raster Surface. Canvas returned by Surface draws directly into pixels.
         * Allocates and zeroes pixel memory. Pixel memory size is imageInfo.height() times
         * rowBytes, or times imageInfo.minRowBytes() if rowBytes is zero.
         * Pixel memory is deleted when Surface is deleted.
         *
         *
         * Surface is returned if all parameters are valid. Valid parameters include:
         *
         *  * info dimensions are greater than zero;
         *  * info contains ColorType and AlphaType supported by raster surface;
         *  * rowBytes is large enough to contain info width pixels of ColorType, or is zero.
         *
         *
         * If rowBytes is zero, a suitable value will be chosen internally.
         *
         * @param imageInfo     width, height, ColorType, AlphaType, ColorSpace,
         * of raster surface; width and height must be greater than zero
         * @param rowBytes      interval from one Surface row to the next; may be zero
         * @param surfaceProps  LCD striping orientation and setting for device independent fonts;
         * may be null
         * @return              new Surface
         */
        fun makeRaster(
            imageInfo: ImageInfo,
            rowBytes: Int,
            surfaceProps: SurfaceProps?
        ): Surface {
            return try {
                Stats.onNativeCall()
                val ptr = interopScope {
                    _nMakeRaster(
                        imageInfo.width,
                        imageInfo.height,
                        imageInfo.colorInfo.colorType.ordinal,
                        imageInfo.colorInfo.alphaType.ordinal,
                        getPtr(imageInfo.colorInfo.colorSpace),
                        rowBytes,
                        toInterop(surfaceProps?.packToIntArray())
                    )
                }
                require(ptr != NullPointer) {
                    "Failed Surface.makeRaster($imageInfo, $rowBytes, $surfaceProps)"
                }
                Surface(ptr)
            } finally {
                reachabilityBarrier(imageInfo.colorInfo.colorSpace)
            }
        }
        /**
         *
         * Allocates raster [Surface].
         *
         *
         * Canvas returned by Surface draws directly into pixels. Allocates and zeroes pixel memory.
         * Pixel memory size is height times width times four. Pixel memory is deleted when Surface is deleted.
         *
         *
         * Internally, sets ImageInfo to width, height, native color type, and ColorAlphaType.PREMUL.
         *
         *
         * Surface is returned if width and height are greater than zero.
         *
         *
         * Use to create Surface that matches PMColor, the native pixel arrangement on the platform.
         * Surface drawn to output device skips converting its pixel format.
         *
         * @param width  pixel column count; must be greater than zero
         * @param height pixel row count; must be greater than zero
         * @return Surface if all parameters are valid; otherwise, null
         * @see [https://fiddle.skia.org/c/@Surface_MakeRasterN32Premul](https://fiddle.skia.org/c/@Surface_MakeRasterN32Premul)
         */
        fun makeRasterN32Premul(width: Int, height: Int): Surface {
            Stats.onNativeCall()
            val ptr = _nMakeRasterN32Premul(width, height)
            require(ptr != NullPointer) { "Failed Surface.makeRasterN32Premul($width, $height)" }
            return Surface(ptr)
        }

        /**
         * Returns Surface without backing pixels. Drawing to Canvas returned from Surface
         * has no effect. Calling makeImageSnapshot() on returned Surface returns null.
         *
         * @param width   one or greater
         * @param height  one or greater
         * @return        Surface if width and height are positive
         *
         * @see [https://fiddle.skia.org/c/@Surface_MakeNull](https://fiddle.skia.org/c/@Surface_MakeNull)
         */
        fun makeNull(width: Int, height: Int): Surface {
            Stats.onNativeCall()
            val ptr = _nMakeNull(width, height)
            require(ptr != NullPointer) { "Failed Surface.makeNull($width, $height)" }
            return Surface(ptr)
        }

        init {
            staticLoad()
        }
    }

    // Keeps resources owned by another module alive for as long as this Surface uses them.
    private val lifetimeOwner: Any?

    /**
     *
     * Returns pixel count in each row; may be zero or greater.
     *
     * @return number of pixel columns
     * @see [https://fiddle.skia.org/c/@Surface_width](https://fiddle.skia.org/c/@Surface_width)
     */
    val width: Int
        get() = try {
            Stats.onNativeCall()
            Surface_nGetWidth(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Returns pixel row count; may be zero or greater.
     *
     * @return number of pixel rows
     * @see [https://fiddle.skia.org/c/@Surface_height](https://fiddle.skia.org/c/@Surface_height)
     */
    val height: Int
        get() = try {
            Stats.onNativeCall()
            Surface_nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Returns an ImageInfo describing the surface.
     *
     * @return ImageInfo describing the surface.
     */
    val imageInfo: ImageInfo
        get() = try {
            ImageInfo.createUsing(
                _ptr = _ptr,
                _nGetImageInfo = ::Surface_nGetImageInfo
            )
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Returns unique value identifying the content of Surface.
     *
     *
     * Returned value changes each time the content changes.
     * Content is changed by drawing, or by calling notifyContentWillChange().
     *
     * @return unique content identifier
     */
    val generationId: Int
        get() = try {
            Stats.onNativeCall()
            _nGenerationId(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Notifies that Surface contents will be changed by code outside of Skia.
     *
     *
     * Subsequent calls to generationID() return a different value.
     *
     * @see [https://fiddle.skia.org/c/@Surface_notifyContentWillChange](https://fiddle.skia.org/c/@Surface_notifyContentWillChange)
     */
    fun notifyContentWillChange(mode: ContentChangeMode) {
        try {
            Stats.onNativeCall()
            _nNotifyContentWillChange(_ptr, mode.ordinal)
        } finally {
            reachabilityBarrier(this)
        }
    }


    /**
     *
     * Returns Canvas that draws into Surface.
     *
     *
     * Subsequent calls return the same Canvas.
     * Canvas returned is managed and owned by Surface, and is deleted when Surface is deleted.
     *
     * @return Canvas for Surface
     */
    val canvas: Canvas
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetCanvas(_ptr)
            if (ptr == NullPointer) throw IllegalArgumentException() else Canvas(ptr, false, this)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Returns a compatible Surface, or null.
     *
     *
     * Returned Surface contains the same raster, GPU, or null properties as the original.
     * Returned Surface does not share the same pixels.
     *
     *
     * Returns null if imageInfo width or height are zero, or if imageInfo is incompatible with Surface.
     *
     * @param imageInfo contains width, height, AlphaType, ColorType, ColorSpace
     * @return compatible SkSurface or null
     * @see [https://fiddle.skia.org/c/@Surface_makeSurface](https://fiddle.skia.org/c/@Surface_makeSurface)
     */
    fun makeSurface(imageInfo: ImageInfo): Surface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nMakeSurfaceI(
                _ptr,
                imageInfo.width,
                imageInfo.height,
                imageInfo.colorInfo.colorType.ordinal,
                imageInfo.colorInfo.alphaType.ordinal,
                getPtr(imageInfo.colorInfo.colorSpace)
            )
            Surface(ptr)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(imageInfo.colorInfo.colorSpace)
        }
    }

    /**
     *
     * Calls makeSurface(ImageInfo) with the same ImageInfo as this surface,
     * but with the specified width and height.
     *
     *
     * Returned Surface contains the same raster, GPU, or null properties as the original.
     * Returned Surface does not share the same pixels.
     *
     *
     * Returns null if imageInfo width or height are zero, or if imageInfo is incompatible with Surface.
     *
     * @param width  pixel column count; must be greater than zero
     * @param height pixel row count; must be greater than zero
     * @return compatible SkSurface or null
     */
    fun makeSurface(width: Int, height: Int): Surface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nMakeSurface(_ptr, width, height)
            Surface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Returns Image capturing Surface contents.
     *
     *
     * Subsequent drawing to Surface contents are not captured.
     * Image allocation is accounted for if Surface was created with SkBudgeted::kYes.
     *
     * @return Image initialized with Surface contents
     * @see [https://fiddle.skia.org/c/@Surface_makeImageSnapshot](https://fiddle.skia.org/c/@Surface_makeImageSnapshot)
     */
    fun makeImageSnapshot(): Image {
        return try {
            Stats.onNativeCall()
            Image(_nMakeImageSnapshot(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Like the no-parameter version, this returns an image of the current surface contents.
     *
     *
     * This variant takes a rectangle specifying the subset of the surface that is of interest.
     * These bounds will be sanitized before being used.
     *
     *
     *  * If bounds extends beyond the surface, it will be trimmed to just the intersection of it and the surface.
     *  * If bounds does not intersect the surface, then this returns null.
     *  * If bounds == the surface, then this is the same as calling the no-parameter variant.
     *
     *
     * @return Image initialized with Surface contents or null
     * @see [https://fiddle.skia.org/c/@Surface_makeImageSnapshot_2](https://fiddle.skia.org/c/@Surface_makeImageSnapshot_2)
     */
    fun makeImageSnapshot(area: IRect): Image? {
        return makeImageSnapshot(area.left, area.top, area.right, area.bottom)
    }

    /**
     *
     * Like the no-parameter version, this returns an image of the current surface contents.
     *
     *
     * This variant takes a rectangle specifying the subset of the surface that is of interest.
     * These bounds will be sanitized before being used.
     *
     *
     *  * If bounds extends beyond the surface, it will be trimmed to just the intersection of it and the surface.
     *  * If bounds does not intersect the surface, then this returns null.
     *  * If bounds == the surface, then this is the same as calling the no-parameter variant.
     *
     *
     * @return Image initialized with Surface contents or null
     * @see [https://fiddle.skia.org/c/@Surface_makeImageSnapshot_2](https://fiddle.skia.org/c/@Surface_makeImageSnapshot_2)
     */
    fun makeImageSnapshot(left: Int, top: Int, right: Int, bottom: Int): Image? {
        return try {
            Stats.onNativeCall()
            Image(
                _nMakeImageSnapshotR(
                    _ptr,
                    left,
                    top,
                    right,
                    bottom
                )
            )
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Draws Surface contents to canvas, with its top-left corner at (x, y).
     *
     *
     * If Paint paint is not null, apply ColorFilter, alpha, ImageFilter, and BlendMode.
     *
     * @param canvas Canvas drawn into
     * @param x      horizontal offset in Canvas
     * @param y      vertical offset in Canvas
     * @param paint  Paint containing BlendMode, ColorFilter, ImageFilter, and so on; or null
     * @see [https://fiddle.skia.org/c/@Surface_draw](https://fiddle.skia.org/c/@Surface_draw)
     */
    fun draw(canvas: Canvas?, x: Int, y: Int, paint: Paint?) {
        draw(canvas, x, y, SamplingMode.DEFAULT, paint)
    }

    /**
     *
     * Draws Surface contents to canvas, with its top-left corner at (x, y).
     *
     *
     * If Paint paint is not null, apply ColorFilter, alpha, ImageFilter, and BlendMode.
     *
     * @param canvas Canvas drawn into
     * @param x      horizontal offset in Canvas
     * @param y      vertical offset in Canvas
     * @param sampling	what technique to use when sampling the surface pixels
     * @param paint  Paint containing BlendMode, ColorFilter, ImageFilter, and so on; or null
     * @see [https://fiddle.skia.org/c/@Surface_draw](https://fiddle.skia.org/c/@Surface_draw)
     */
    fun draw(canvas: Canvas?, x: Int, y: Int, samplingMode: SamplingMode, paint: Paint?) {
        try {
            Stats.onNativeCall()
            _nDraw(_ptr, getPtr(canvas), x.toFloat(), y.toFloat(), samplingMode._packedInt1(), samplingMode._packedInt2(), getPtr(paint))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(canvas)
            reachabilityBarrier(paint)
        }
    }

    fun peekPixels(pixmap: Pixmap): Boolean {
        return try {
            Stats.onNativeCall()
            _nPeekPixels(
                _ptr,
                getPtr(pixmap)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pixmap)
        }
    }

    fun readPixels(pixmap: Pixmap?, srcX: Int, srcY: Int): Boolean {
        return try {
            Stats.onNativeCall()
            _nReadPixelsToPixmap(
                _ptr,
                getPtr(pixmap),
                srcX,
                srcY
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pixmap)
        }
    }

    /**
     *
     * Copies Rect of pixels from Surface into bitmap.
     *
     *
     * Source Rect corners are (srcX, srcY) and Surface (width(), height()).
     * Destination Rect corners are (0, 0) and (bitmap.width(), bitmap.height()).
     * Copies each readable pixel intersecting both rectangles, without scaling,
     * converting to bitmap.colorType() and bitmap.alphaType() if required.
     *
     *
     * Pixels are readable when Surface is raster, or backed by a GPU.
     *
     *
     * The destination pixel storage must be allocated by the caller.
     *
     *
     * Pixel values are converted only if ColorType and AlphaType do not match.
     * Only pixels within both source and destination rectangles are copied.
     * dst contents outside Rect intersection are unchanged.
     *
     *
     * Pass negative values for srcX or srcY to offset pixels across or down destination.
     *
     *
     * Does not copy, and returns false if:
     *
     *
     *  * Source and destination rectangles do not intersect.
     *  * Surface pixels could not be converted to dst.colorType() or dst.alphaType().
     *  * dst pixels could not be allocated.
     *  * dst.rowBytes() is too small to contain one row of pixels.
     *
     *
     * @param bitmap  storage for pixels copied from SkSurface
     * @param srcX    offset into readable pixels on x-axis; may be negative
     * @param srcY    offset into readable pixels on y-axis; may be negative
     * @return        true if pixels were copied
     * @see [https://fiddle.skia.org/c/@Surface_readPixels_3](https://fiddle.skia.org/c/@Surface_readPixels_3)
     */
    fun readPixels(bitmap: Bitmap?, srcX: Int, srcY: Int): Boolean {
        return try {
            Stats.onNativeCall()
            Surface_nReadPixels(
                _ptr,
                getPtr(bitmap),
                srcX,
                srcY
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(bitmap)
        }
    }

    fun writePixels(pixmap: Pixmap?, x: Int, y: Int) {
        try {
            Stats.onNativeCall()
            _nWritePixelsFromPixmap(_ptr, getPtr(pixmap), x, y)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pixmap)
        }
    }

    /**
     *
     * Copies Rect of pixels from the src Bitmap to the Surface.
     *
     *
     * Source Rect corners are (0, 0) and (src.width(), src.height()).
     * Destination Rect corners are (dstX, dstY) and (dstX + Surface width(), dstY + Surface height()).
     *
     *
     * Copies each readable pixel intersecting both rectangles, without scaling,
     * converting to Surface colorType() and Surface alphaType() if required.
     *
     * @param bitmap storage for pixels to copy to Surface
     * @param x      x-axis position relative to Surface to begin copy; may be negative
     * @param y      y-axis position relative to Surface to begin copy; may be negative
     * @see [https://fiddle.skia.org/c/@Surface_writePixels_2](https://fiddle.skia.org/c/@Surface_writePixels_2)
     */
    fun writePixels(bitmap: Bitmap?, x: Int, y: Int) {
        try {
            Stats.onNativeCall()
            Surface_nWritePixels(_ptr, getPtr(bitmap), x, y)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(bitmap)
        }
    }


    /**
     *
     * May return true if the caller is the only owner.
     *
     *
     * Ensures that all previous owner's actions are complete.
     */
    val isUnique: Boolean
        get() = try {
            Stats.onNativeCall()
            _nUnique(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    @InternalSkikoApi
    constructor(ptr: NativePointer, lifetimeOwner: Any? = null) : super(ptr) {
        this.lifetimeOwner = lifetimeOwner
    }
}

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetWidth")
private external fun Surface_nGetWidth(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetHeight")
private external fun Surface_nGetHeight(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetImageInfo")
private external fun Surface_nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nReadPixels")
private external fun Surface_nReadPixels(ptr: NativePointer, bitmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Surface__1nWritePixels")
private external fun Surface_nWritePixels(ptr: NativePointer, bitmapPtr: NativePointer, x: Int, y: Int)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRasterDirect")
private external fun _nMakeRasterDirect(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixelsPtr: NativePointer,
    rowBytes: Int,
    surfaceProps: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRasterDirectWithPixmap")
private external fun _nMakeRasterDirectWithPixmap(pixmapPtr: NativePointer, surfaceProps: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRaster")
private external fun _nMakeRaster(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    rowBytes: Int,
    surfaceProps: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRasterN32Premul")
private external fun _nMakeRasterN32Premul(width: Int, height: Int): NativePointer



@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeNull")
private external fun _nMakeNull(width: Int, height: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGenerationId")
private external fun _nGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Surface__1nNotifyContentWillChange")
private external fun _nNotifyContentWillChange(ptr: NativePointer, mode: Int)


@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetCanvas")
private external fun _nGetCanvas(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeSurfaceI")
private external fun _nMakeSurfaceI(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeSurface")
private external fun _nMakeSurface(ptr: NativePointer, width: Int, height: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeImageSnapshot")
private external fun _nMakeImageSnapshot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeImageSnapshotR")
private external fun _nMakeImageSnapshotR(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nDraw")
private external fun _nDraw(ptr: NativePointer, canvasPtr: NativePointer, x: Float, y: Float, samplingModeValue1: Int, samplingModeValue2: Int, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nPeekPixels")
private external fun _nPeekPixels(ptr: NativePointer, pixmapPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Surface__1nReadPixelsToPixmap")
private external fun _nReadPixelsToPixmap(ptr: NativePointer, pixmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Surface__1nWritePixelsFromPixmap")
private external fun _nWritePixelsFromPixmap(ptr: NativePointer, pixmapPtr: NativePointer, x: Int, y: Int)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nUnique")
private external fun _nUnique(ptr: NativePointer): Boolean
