package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.annotations.Contract
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.IllegalArgumentException
import java.lang.ref.Reference

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
            pixelsPtr: Long,
            rowBytes: Long
        ): Surface {
            return makeRasterDirect(imageInfo, pixelsPtr, rowBytes, null)
        }

        fun makeRasterDirect(
            pixmap: Pixmap,
            surfaceProps: SurfaceProps?
        ): Surface {
            return try {
                assert(pixmap != null) { "Can’t makeRasterDirect with pixmap == null" }
                Stats.onNativeCall()
                val ptr = _nMakeRasterDirectWithPixmap(
                    Native.Companion.getPtr(pixmap), surfaceProps
                )
                require(ptr != 0L) {
                    String.format(
                        "Failed Surface.makeRasterDirect(%s, %s)",
                        pixmap,
                        surfaceProps
                    )
                }
                Surface(ptr)
            } finally {
                Reference.reachabilityFence(pixmap)
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
            pixelsPtr: Long,
            rowBytes: Long,
            surfaceProps: SurfaceProps?
        ): Surface {
            return try {
                assert(imageInfo != null) { "Can’t makeRasterDirect with imageInfo == null" }
                Stats.onNativeCall()
                val ptr = _nMakeRasterDirect(
                    imageInfo.width,
                    imageInfo.height,
                    imageInfo.colorInfo.colorType.ordinal,
                    imageInfo.colorInfo.alphaType.ordinal,
                    Native.getPtr(imageInfo.colorInfo.colorSpace),
                    pixelsPtr,
                    rowBytes,
                    surfaceProps
                )
                require(ptr != 0L) {
                    String.format(
                        "Failed Surface.makeRasterDirect(%s, %d, %d, %s)",
                        imageInfo,
                        pixelsPtr,
                        rowBytes,
                        surfaceProps
                    )
                }
                Surface(ptr)
            } finally {
                Reference.reachabilityFence(imageInfo.colorInfo.colorSpace)
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
            return makeRaster(imageInfo, 0, null)
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
            rowBytes: Long
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
            rowBytes: Long,
            surfaceProps: SurfaceProps?
        ): Surface {
            return try {
                assert(imageInfo != null) { "Can’t makeRaster with imageInfo == null" }
                Stats.onNativeCall()
                val ptr = _nMakeRaster(
                    imageInfo.width,
                    imageInfo.height,
                    imageInfo.colorInfo.colorType.ordinal,
                    imageInfo.colorInfo.alphaType.ordinal,
                    Native.getPtr(imageInfo.colorInfo.colorSpace),
                    rowBytes,
                    surfaceProps
                )
                require(ptr != 0L) {
                    String.format(
                        "Failed Surface.makeRaster(%s, %d, %s)",
                        imageInfo,
                        rowBytes,
                        surfaceProps
                    )
                }
                Surface(ptr)
            } finally {
                Reference.reachabilityFence(imageInfo.colorInfo.colorSpace)
            }
        }
        /**
         *
         * Wraps a GPU-backed buffer into [Surface].
         *
         *
         * Caller must ensure backendRenderTarget is valid for the lifetime of returned [Surface].
         *
         *
         * [Surface] is returned if all parameters are valid. backendRenderTarget is valid if its pixel
         * configuration agrees with colorSpace and context;
         * for instance, if backendRenderTarget has an sRGB configuration, then context must support sRGB,
         * and colorSpace must be present. Further, backendRenderTarget width and height must not exceed
         * context capabilities, and the context must be able to support back-end render targets.
         *
         * @param context       GPU context
         * @param rt            texture residing on GPU
         * @param origin        surfaceOrigin pins either the top-left or the bottom-left corner to the origin.
         * @param colorFormat   color format
         * @param colorSpace    range of colors; may be null
         * @param surfaceProps  LCD striping orientation and setting for device independent fonts; may be null
         * @return              Surface if all parameters are valid; otherwise, null
         * @see [https://fiddle.skia.org/c/@Surface_MakeFromBackendTexture](https://fiddle.skia.org/c/@Surface_MakeFromBackendTexture)
         */
        /**
         *
         * Wraps a GPU-backed buffer into [Surface].
         *
         *
         * Caller must ensure backendRenderTarget is valid for the lifetime of returned [Surface].
         *
         *
         * [Surface] is returned if all parameters are valid. backendRenderTarget is valid if its pixel
         * configuration agrees with colorSpace and context;
         * for instance, if backendRenderTarget has an sRGB configuration, then context must support sRGB,
         * and colorSpace must be present. Further, backendRenderTarget width and height must not exceed
         * context capabilities, and the context must be able to support back-end render targets.
         *
         * @param context       GPU context
         * @param rt            texture residing on GPU
         * @param origin        surfaceOrigin pins either the top-left or the bottom-left corner to the origin.
         * @param colorFormat   color format
         * @param colorSpace    range of colors; may be null
         * @return              Surface if all parameters are valid; otherwise, null
         * @see [https://fiddle.skia.org/c/@Surface_MakeFromBackendTexture](https://fiddle.skia.org/c/@Surface_MakeFromBackendTexture)
         */
        @JvmOverloads
        fun makeFromBackendRenderTarget(
            context: DirectContext,
            rt: BackendRenderTarget,
            origin: SurfaceOrigin,
            colorFormat: SurfaceColorFormat,
            colorSpace: ColorSpace?,
            surfaceProps: SurfaceProps? = null
        ): Surface {
            return try {
                assert(context != null) { "Can’t makeFromBackendRenderTarget with context == null" }
                assert(rt != null) { "Can’t makeFromBackendRenderTarget with rt == null" }
                assert(origin != null) { "Can’t makeFromBackendRenderTarget with origin == null" }
                assert(colorFormat != null) { "Can’t makeFromBackendRenderTarget with colorFormat == null" }
                Stats.onNativeCall()
                val ptr = _nMakeFromBackendRenderTarget(
                    Native.Companion.getPtr(context),
                    Native.Companion.getPtr(rt),
                    origin.ordinal,
                    colorFormat.ordinal,
                    Native.Companion.getPtr(colorSpace),
                    surfaceProps
                )
                require(ptr != 0L) {
                    String.format(
                        "Failed Surface.makeFromBackendRenderTarget(%s, %s, %s, %s, %s)",
                        context,
                        rt,
                        origin,
                        colorFormat,
                        colorSpace
                    )
                }
                Surface(ptr, context, rt)
            } finally {
                Reference.reachabilityFence(context)
                Reference.reachabilityFence(rt)
                Reference.reachabilityFence(colorSpace)
            }
        }

        fun makeFromMTKView(
            context: DirectContext,
            mtkViewPtr: Long,
            origin: SurfaceOrigin,
            sampleCount: Int,
            colorFormat: SurfaceColorFormat,
            colorSpace: ColorSpace?,
            surfaceProps: SurfaceProps?
        ): Surface {
            return try {
                assert(context != null) { "Can’t makeFromBackendRenderTarget with context == null" }
                assert(origin != null) { "Can’t makeFromBackendRenderTarget with origin == null" }
                assert(colorFormat != null) { "Can’t makeFromBackendRenderTarget with colorFormat == null" }
                Stats.onNativeCall()
                val ptr = _nMakeFromMTKView(
                    Native.Companion.getPtr(context),
                    mtkViewPtr,
                    origin.ordinal,
                    sampleCount,
                    colorFormat.ordinal,
                    Native.Companion.getPtr(colorSpace),
                    surfaceProps
                )
                require(ptr != 0L) {
                    String.format(
                        "Failed Surface.makeFromMTKView(%s, %s, %s, %s, %s, %s)",
                        context,
                        mtkViewPtr,
                        origin,
                        colorFormat,
                        colorSpace,
                        surfaceProps
                    )
                }
                Surface(ptr, context)
            } finally {
                Reference.reachabilityFence(context)
                Reference.reachabilityFence(colorSpace)
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
            require(ptr != 0L) { String.format("Failed Surface.makeRasterN32Premul(%d, %d)", width, height) }
            return Surface(ptr)
        }

        /**
         *
         * Returns Surface on GPU indicated by context. Allocates memory for
         * pixels, based on the width, height, and ColorType in ImageInfo.
         * describes the pixel format in ColorType, and transparency in
         * AlphaType, and color matching in ColorSpace.
         *
         * @param context               GPU context
         * @param budgeted              selects whether allocation for pixels is tracked by context
         * @param imageInfo             width, height, ColorType, AlphaType, ColorSpace;
         * width, or height, or both, may be zero
         * @return                      new SkSurface
         */
        fun makeRenderTarget(
            context: DirectContext,
            budgeted: Boolean,
            imageInfo: ImageInfo
        ): Surface {
            return makeRenderTarget(context, budgeted, imageInfo, 0, SurfaceOrigin.BOTTOM_LEFT, null, false)
        }

        /**
         *
         * Returns Surface on GPU indicated by context. Allocates memory for
         * pixels, based on the width, height, and ColorType in ImageInfo.
         * describes the pixel format in ColorType, and transparency in
         * AlphaType, and color matching in ColorSpace.
         *
         *
         * sampleCount requests the number of samples per pixel.
         * Pass zero to disable multi-sample anti-aliasing.  The request is rounded
         * up to the next supported count, or rounded down if it is larger than the
         * maximum supported count.
         *
         * @param context               GPU context
         * @param budgeted              selects whether allocation for pixels is tracked by context
         * @param imageInfo             width, height, ColorType, AlphaType, ColorSpace;
         * width, or height, or both, may be zero
         * @param sampleCount           samples per pixel, or 0 to disable full scene anti-aliasing
         * @param surfaceProps          LCD striping orientation and setting for device independent
         * fonts; may be null
         * @return                      new SkSurface
         */
        fun makeRenderTarget(
            context: DirectContext,
            budgeted: Boolean,
            imageInfo: ImageInfo,
            sampleCount: Int,
            surfaceProps: SurfaceProps?
        ): Surface {
            return makeRenderTarget(
                context,
                budgeted,
                imageInfo,
                sampleCount,
                SurfaceOrigin.BOTTOM_LEFT,
                surfaceProps,
                false
            )
        }

        /**
         *
         * Returns Surface on GPU indicated by context. Allocates memory for
         * pixels, based on the width, height, and ColorType in ImageInfo.
         * describes the pixel format in ColorType, and transparency in
         * AlphaType, and color matching in ColorSpace.
         *
         *
         * sampleCount requests the number of samples per pixel.
         * Pass zero to disable multi-sample anti-aliasing.  The request is rounded
         * up to the next supported count, or rounded down if it is larger than the
         * maximum supported count.
         *
         * @param context               GPU context
         * @param budgeted              selects whether allocation for pixels is tracked by context
         * @param imageInfo             width, height, ColorType, AlphaType, ColorSpace;
         * width, or height, or both, may be zero
         * @param sampleCount           samples per pixel, or 0 to disable full scene anti-aliasing
         * @param origin                pins either the top-left or the bottom-left corner to the origin.
         * @param surfaceProps          LCD striping orientation and setting for device independent
         * fonts; may be null
         * @return                      new SkSurface
         */
        fun makeRenderTarget(
            context: DirectContext,
            budgeted: Boolean,
            imageInfo: ImageInfo,
            sampleCount: Int,
            origin: SurfaceOrigin,
            surfaceProps: SurfaceProps?
        ): Surface {
            return makeRenderTarget(context, budgeted, imageInfo, sampleCount, origin, surfaceProps, false)
        }

        /**
         *
         * Returns Surface on GPU indicated by context. Allocates memory for
         * pixels, based on the width, height, and ColorType in ImageInfo.
         * describes the pixel format in ColorType, and transparency in
         * AlphaType, and color matching in ColorSpace.
         *
         *
         * sampleCount requests the number of samples per pixel.
         * Pass zero to disable multi-sample anti-aliasing.  The request is rounded
         * up to the next supported count, or rounded down if it is larger than the
         * maximum supported count.
         *
         *
         * shouldCreateWithMips hints that Image returned by [.makeImageSnapshot] is mip map.
         *
         * @param context               GPU context
         * @param budgeted              selects whether allocation for pixels is tracked by context
         * @param imageInfo             width, height, ColorType, AlphaType, ColorSpace;
         * width, or height, or both, may be zero
         * @param sampleCount           samples per pixel, or 0 to disable full scene anti-aliasing
         * @param origin                pins either the top-left or the bottom-left corner to the origin.
         * @param surfaceProps          LCD striping orientation and setting for device independent
         * fonts; may be null
         * @param shouldCreateWithMips  hint that SkSurface will host mip map images
         * @return                      new SkSurface
         */
        fun makeRenderTarget(
            context: DirectContext,
            budgeted: Boolean,
            imageInfo: ImageInfo,
            sampleCount: Int,
            origin: SurfaceOrigin,
            surfaceProps: SurfaceProps?,
            shouldCreateWithMips: Boolean
        ): Surface {
            return try {
                assert(context != null) { "Can’t makeFromBackendRenderTarget with context == null" }
                assert(imageInfo != null) { "Can’t makeFromBackendRenderTarget with imageInfo == null" }
                assert(origin != null) { "Can’t makeFromBackendRenderTarget with origin == null" }
                Stats.onNativeCall()
                val ptr = _nMakeRenderTarget(
                    Native.getPtr(context),
                    budgeted,
                    imageInfo.width,
                    imageInfo.height,
                    imageInfo.colorInfo.colorType.ordinal,
                    imageInfo.colorInfo.alphaType.ordinal,
                    Native.Companion.getPtr(imageInfo.colorInfo.colorSpace),
                    sampleCount,
                    origin.ordinal,
                    surfaceProps,
                    shouldCreateWithMips
                )
                require(ptr != 0L) {
                    String.format(
                        "Failed Surface.makeRenderTarget(%s, %b, %s, %d, %s, %s, %b)",
                        context,
                        budgeted,
                        imageInfo,
                        sampleCount,
                        origin,
                        surfaceProps,
                        shouldCreateWithMips
                    )
                }
                Surface(ptr, context)
            } finally {
                Reference.reachabilityFence(context)
                Reference.reachabilityFence(imageInfo.colorInfo.colorSpace)
            }
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
            require(ptr != 0L) { String.format("Failed Surface.makeNull(%d, %d)", width, height) }
            return Surface(ptr)
        }

        @JvmStatic external fun _nMakeRasterDirect(
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            pixelsPtr: Long,
            rowBytes: Long,
            surfaceProps: SurfaceProps?
        ): Long

        @JvmStatic external fun _nMakeRasterDirectWithPixmap(pixmapPtr: Long, surfaceProps: SurfaceProps?): Long
        @JvmStatic external fun _nMakeRaster(
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            rowBytes: Long,
            surfaceProps: SurfaceProps?
        ): Long

        @JvmStatic external fun _nMakeRasterN32Premul(width: Int, height: Int): Long
        @JvmStatic external fun _nMakeFromBackendRenderTarget(
            pContext: Long,
            pBackendRenderTarget: Long,
            surfaceOrigin: Int,
            colorType: Int,
            colorSpacePtr: Long,
            surfaceProps: SurfaceProps?
        ): Long

        @JvmStatic external fun _nMakeFromMTKView(
            contextPtr: Long,
            mtkViewPtr: Long,
            surfaceOrigin: Int,
            sampleCount: Int,
            colorType: Int,
            colorSpacePtr: Long,
            surfaceProps: SurfaceProps?
        ): Long

        @JvmStatic external fun _nMakeRenderTarget(
            contextPtr: Long,
            budgeted: Boolean,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            sampleCount: Int,
            surfaceOrigin: Int,
            surfaceProps: SurfaceProps?,
            shouldCreateWithMips: Boolean
        ): Long

        @JvmStatic external fun _nMakeNull(width: Int, height: Int): Long
        @JvmStatic external fun _nGetWidth(ptr: Long): Int
        @JvmStatic external fun _nGetHeight(ptr: Long): Int
        @JvmStatic external fun _nGetImageInfo(ptr: Long): ImageInfo
        @JvmStatic external fun _nGenerationId(ptr: Long): Int
        @JvmStatic external fun _nNotifyContentWillChange(ptr: Long, mode: Int)
        @JvmStatic external fun _nGetRecordingContext(ptr: Long): Long
        @JvmStatic external fun _nGetCanvas(ptr: Long): Long
        @JvmStatic external fun _nMakeSurfaceI(
            ptr: Long,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long
        ): Long

        @JvmStatic external fun _nMakeSurface(ptr: Long, width: Int, height: Int): Long
        @JvmStatic external fun _nMakeImageSnapshot(ptr: Long): Long
        @JvmStatic external fun _nMakeImageSnapshotR(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Long
        @JvmStatic external fun _nDraw(ptr: Long, canvasPtr: Long, x: Float, y: Float, paintPtr: Long)
        @JvmStatic external fun _nPeekPixels(ptr: Long, pixmapPtr: Long): Boolean
        @JvmStatic external fun _nReadPixelsToPixmap(ptr: Long, pixmapPtr: Long, srcX: Int, srcY: Int): Boolean
        @JvmStatic external fun _nReadPixels(ptr: Long, bitmapPtr: Long, srcX: Int, srcY: Int): Boolean
        @JvmStatic external fun _nWritePixelsFromPixmap(ptr: Long, pixmapPtr: Long, x: Int, y: Int)
        @JvmStatic external fun _nWritePixels(ptr: Long, bitmapPtr: Long, x: Int, y: Int)
        @JvmStatic external fun _nFlushAndSubmit(ptr: Long, syncCpu: Boolean)
        @JvmStatic external fun _nFlush(ptr: Long)
        @JvmStatic external fun _nUnique(ptr: Long): Boolean

        init {
            staticLoad()
        }
    }

    @ApiStatus.Internal
    val _context: DirectContext?

    @ApiStatus.Internal
    val _renderTarget: BackendRenderTarget?

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
            _nGetWidth(_ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     *
     * Returns an ImageInfo describing the surface.
     *
     * @return ImageInfo describing the surface.
     */
    val imageInfo: ImageInfo
        get() = try {
            Stats.onNativeCall()
            _nGetImageInfo(_ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
        }
    }

    /**
     *
     * Returns the recording context being used by the Surface.
     *
     * @return the recording context, if available; null otherwise
     */
    val recordingContext: DirectContext?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetRecordingContext(_ptr)
            if (ptr == 0L) null else DirectContext(ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            if (ptr == 0L) throw IllegalArgumentException() else Canvas(ptr, false, this)
        } finally {
            Reference.reachabilityFence(this)
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
                Native.Companion.getPtr(imageInfo.colorInfo.colorSpace)
            )
            Surface(ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            org.jetbrains.skija.Image(_nMakeImageSnapshot(_ptr))
        } finally {
            Reference.reachabilityFence(this)
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
        return try {
            Stats.onNativeCall()
            org.jetbrains.skija.Image(
                _nMakeImageSnapshotR(
                    _ptr,
                    area.left,
                    area.top,
                    area.right,
                    area.bottom
                )
            )
        } finally {
            Reference.reachabilityFence(this)
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
        try {
            Stats.onNativeCall()
            _nDraw(_ptr, Native.Companion.getPtr(canvas), x.toFloat(), y.toFloat(), Native.Companion.getPtr(paint))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(canvas)
            Reference.reachabilityFence(paint)
        }
    }

    fun peekPixels(pixmap: Pixmap): Boolean {
        return try {
            Stats.onNativeCall()
            _nPeekPixels(
                _ptr,
                Native.Companion.getPtr(pixmap)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(pixmap)
        }
    }

    fun readPixels(pixmap: Pixmap?, srcX: Int, srcY: Int): Boolean {
        return try {
            Stats.onNativeCall()
            _nReadPixelsToPixmap(
                _ptr,
                Native.Companion.getPtr(pixmap),
                srcX,
                srcY
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(pixmap)
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
            _nReadPixels(
                _ptr,
                Native.Companion.getPtr(bitmap),
                srcX,
                srcY
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(bitmap)
        }
    }

    fun writePixels(pixmap: Pixmap?, x: Int, y: Int) {
        try {
            Stats.onNativeCall()
            _nWritePixelsFromPixmap(_ptr, Native.Companion.getPtr(pixmap), x, y)
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(pixmap)
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
            _nWritePixels(_ptr, Native.Companion.getPtr(bitmap), x, y)
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(bitmap)
        }
    }

    /**
     *
     * Call to ensure all reads/writes of the surface have been issued to the underlying 3D API.
     *
     *
     * Skia will correctly order its own draws and pixel operations.
     * This must to be used to ensure correct ordering when the surface backing store is accessed
     * outside Skia (e.g. direct use of the 3D API or a windowing system).
     * DirectContext has additional flush and submit methods that apply to all surfaces and images created from
     * a DirectContext.
     */
    fun flushAndSubmit() {
        try {
            Stats.onNativeCall()
            _nFlushAndSubmit(_ptr, false)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     *
     * Call to ensure all reads/writes of the surface have been issued to the underlying 3D API.
     *
     *
     * Skia will correctly order its own draws and pixel operations.
     * This must to be used to ensure correct ordering when the surface backing store is accessed
     * outside Skia (e.g. direct use of the 3D API or a windowing system).
     * DirectContext has additional flush and submit methods that apply to all surfaces and images created from
     * a DirectContext.
     *
     * @param syncCpu a flag determining if cpu should be synced
     */
    fun flushAndSubmit(syncCpu: Boolean) {
        try {
            Stats.onNativeCall()
            _nFlushAndSubmit(_ptr, syncCpu)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun flush() {
        try {
            Stats.onNativeCall()
            _nFlush(_ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
        }

    @ApiStatus.Internal
    constructor(ptr: Long) : super(ptr) {
        _context = null
        _renderTarget = null
    }

    @ApiStatus.Internal
    constructor(ptr: Long, context: DirectContext?) : super(ptr) {
        _context = context
        _renderTarget = null
    }

    @ApiStatus.Internal
    constructor(ptr: Long, context: DirectContext?, renderTarget: BackendRenderTarget?) : super(ptr) {
        _context = context
        _renderTarget = renderTarget
    }
}