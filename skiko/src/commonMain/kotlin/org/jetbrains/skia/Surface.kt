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
        fun makeFromBackendRenderTarget(
            context: DirectContext,
            rt: BackendRenderTarget,
            origin: SurfaceOrigin,
            colorFormat: SurfaceColorFormat,
            colorSpace: ColorSpace?,
            surfaceProps: SurfaceProps? = null
        ): Surface? {
            return try {
                Stats.onNativeCall()
                val ptr = interopScope {
                    _nMakeFromBackendRenderTarget(
                        getPtr(context),
                        getPtr(rt),
                        origin.ordinal,
                        colorFormat.ordinal,
                        getPtr(colorSpace),
                        toInterop(surfaceProps?.packToIntArray())
                    )
                }
                if (ptr == NullPointer)
                    null
                else
                    Surface(ptr, context, rt)
            } finally {
                reachabilityBarrier(context)
                reachabilityBarrier(rt)
                reachabilityBarrier(colorSpace)
            }
        }

        fun makeFromMTKView(
            context: DirectContext,
            mtkViewPtr: NativePointer,
            origin: SurfaceOrigin,
            sampleCount: Int,
            colorFormat: SurfaceColorFormat,
            colorSpace: ColorSpace?,
            surfaceProps: SurfaceProps?
        ): Surface {
            return try {
                Stats.onNativeCall()
                val ptr = interopScope {
                    _nMakeFromMTKView(
                        getPtr(context),
                        mtkViewPtr,
                        origin.ordinal,
                        sampleCount,
                        colorFormat.ordinal,
                        getPtr(colorSpace),
                        toInterop(surfaceProps?.packToIntArray())
                    )
                }
                require(ptr != NullPointer) {
                    "Failed Surface.makeFromMTKView($context, $mtkViewPtr $origin, $colorFormat, $surfaceProps)"
                }
                Surface(ptr, context)
            } finally {
                reachabilityBarrier(context)
                reachabilityBarrier(colorSpace)
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
                Stats.onNativeCall()
                val ptr = interopScope {
                    _nMakeRenderTarget(
                        getPtr(context),
                        budgeted,
                        imageInfo.width,
                        imageInfo.height,
                        imageInfo.colorInfo.colorType.ordinal,
                        imageInfo.colorInfo.alphaType.ordinal,
                        getPtr(imageInfo.colorInfo.colorSpace),
                        sampleCount,
                        origin.ordinal,
                        toInterop(surfaceProps?.packToIntArray()),
                        shouldCreateWithMips
                    )
                }
                require(ptr != NullPointer) {
                    "Failed Surface.makeRenderTarget($context, $budgeted, $imageInfo, $sampleCount, $origin, $surfaceProps, $shouldCreateWithMips)"
                }
                Surface(ptr, context)
            } finally {
                reachabilityBarrier(context)
                reachabilityBarrier(imageInfo.colorInfo.colorSpace)
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
            require(ptr != NullPointer) { "Failed Surface.makeNull($width, $height)" }
            return Surface(ptr)
        }

        init {
            staticLoad()
        }
    }

    internal val _context: DirectContext?

    internal val _renderTarget: BackendRenderTarget?

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
     * Returns the recording context being used by the Surface.
     *
     * The returned context is borrowed from the Surface and must not be closed.
     *
     * @return the recording context, if available; null otherwise
     */
    val recordingContext: DirectContext?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetSurfaceRecordingContext(_ptr)
            if (ptr == NullPointer) null else DirectContext(ptr, managed = false)
        } finally {
            reachabilityBarrier(this)
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
        _context?.flushAndSubmit(this)
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
        _context?.flushAndSubmit(this, syncCpu)
    }

    fun flush() {
        _context?.flush(this)
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
    constructor(ptr: NativePointer) : super(ptr) {
        _context = null
        _renderTarget = null
    }

    internal constructor(ptr: NativePointer, context: DirectContext?) : super(ptr) {
        _context = context
        _renderTarget = null
    }

    internal constructor(ptr: NativePointer, context: DirectContext?, managed: Boolean) : super(ptr, managed) {
        _context = context
        _renderTarget = null
    }

    internal constructor(ptr: NativePointer, context: DirectContext?, renderTarget: BackendRenderTarget?) : super(ptr) {
        _context = context
        _renderTarget = renderTarget
    }
}

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetWidth")
// No ForceNativeThreadState: returns the stored surface width, so it is short and cannot block.
// Native call path: SkSurface::width
private external fun Surface_nGetWidth(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetHeight")
// No ForceNativeThreadState: returns the stored surface height, so it is short and cannot block.
// Native call path: SkSurface::height
private external fun Surface_nGetHeight(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetImageInfo")
// No ForceNativeThreadState: copies the stored image info and takes a reference to its color space, so it is short and
// cannot block.
// Native call path: SkSurface::imageInfo
private external fun Surface_nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nReadPixels")
// Needs ForceNativeThreadState: on a GPU surface it reads pixels back through Metal and waits for the GPU, and on a
// raster surface it converts every pixel.
// Native call path: SkSurface::readPixels -> Device::onReadPixels -> SurfaceContext::readPixels ->
//   GrMtlGpu::onReadPixels
@ForceNativeThreadState
private external fun Surface_nReadPixels(ptr: NativePointer, bitmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Surface__1nWritePixels")
// Needs ForceNativeThreadState: on a GPU surface it uploads pixels through Metal, copying the surface first if needed,
// and on a raster surface it converts every pixel.
// Native call path: SkSurface::writePixels -> Device::onWritePixels -> SurfaceContext::writePixels -> GrMtlGpu
@ForceNativeThreadState
private external fun Surface_nWritePixels(ptr: NativePointer, bitmapPtr: NativePointer, x: Int, y: Int)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRasterDirect")
// Needs ForceNativeThreadState: wraps caller-owned pixel memory as a surface, and first use of a shared static can wait
// for another thread.
// Native call path: SkSurfaces::WrapPixels -> skcpu::Recorder::TODO -> __cxa_guard_acquire
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: wraps caller-owned pixel memory as a surface, and first use of a shared static can wait
// for another thread.
// Native call path: SkSurfaces::WrapPixels -> skcpu::Recorder::TODO -> __cxa_guard_acquire
@ForceNativeThreadState
private external fun _nMakeRasterDirectWithPixmap(pixmapPtr: NativePointer, surfaceProps: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRaster")
// Needs ForceNativeThreadState: allocates and zero-fills a pixel buffer, so its time grows with the surface width and
// height.
// Native call path: SkSurfaces::Raster -> SkMallocPixelRef::MakeAllocate
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: allocates and zero-fills a pixel buffer, so its time grows with the surface width and
// height.
// Native call path: SkSurfaces::Raster -> SkMallocPixelRef::MakeAllocate
@ForceNativeThreadState
private external fun _nMakeRasterN32Premul(width: Int, height: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeFromBackendRenderTarget")
// Needs ForceNativeThreadState: wraps an existing Metal render target, which reads the Metal texture and builds a GPU
// surface.
// Native call path: SkSurfaces::WrapBackendRenderTarget -> GrProxyProvider::wrapBackendRenderTarget -> GrMtlGpu
@ForceNativeThreadState
private external fun _nMakeFromBackendRenderTarget(
    pContext: NativePointer,
    pBackendRenderTarget: NativePointer,
    surfaceOrigin: Int,
    colorType: Int,
    colorSpacePtr: NativePointer,
    surfaceProps: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeFromMTKView")
// Needs ForceNativeThreadState: sends Objective-C messages to a MetalKit view to read its pixel format and size, then
// builds a GPU surface.
// Native call path: SkSurfaces::WrapMTKView
@ForceNativeThreadState
private external fun _nMakeFromMTKView(
    contextPtr: NativePointer,
    mtkViewPtr: NativePointer,
    surfaceOrigin: Int,
    sampleCount: Int,
    colorType: Int,
    colorSpacePtr: NativePointer,
    surfaceProps: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeRenderTarget")
// Needs ForceNativeThreadState: creates a GPU surface and records a command to clear it.
// Native call path: SkSurfaces::RenderTarget -> GrRecordingContext::createDevice
@ForceNativeThreadState
private external fun _nMakeRenderTarget(
    contextPtr: NativePointer,
    budgeted: Boolean,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    sampleCount: Int,
    surfaceOrigin: Int,
    surfaceProps: InteropPointer,
    shouldCreateWithMips: Boolean
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeNull")
// No ForceNativeThreadState: makes an empty placeholder surface that stores only two numbers and no pixels, so it is
// short and cannot block.
// Native call path: SkSurfaces::Null
private external fun _nMakeNull(width: Int, height: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGenerationId")
// No ForceNativeThreadState: returns a stored id, or takes the next id from a shared counter in one atomic step, so it
// is short and cannot block.
// Native call path: SkSurface::generationID -> SkSurface_Base::newGenerationID
private external fun _nGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Surface__1nNotifyContentWillChange")
// Needs ForceNativeThreadState: may copy the whole pixel buffer or replace the GPU backing before drawing; pixel-copy
// time grows with the surface size.
// Native call path: SkSurface::notifyContentWillChange -> SkSurface_Base::aboutToDraw -> onCopyOnWrite
@ForceNativeThreadState
private external fun _nNotifyContentWillChange(ptr: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetSurfaceRecordingContext")
// No ForceNativeThreadState: returns a stored pointer to the recording context without doing any work, so it is short
// and cannot block.
// Native call path: SkSurface::recordingContext -> SkSurface_Ganesh::onGetRecordingContext
private external fun _nGetSurfaceRecordingContext(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nGetCanvas")
// Needs ForceNativeThreadState: with capture enabled, may grow a list that moves every tracked canvas, so its time
// grows with the canvas count.
// Native call path: SkSurface::getCanvas -> SkCaptureManager::makeCaptureCanvas -> TArray::emplace_back
@ForceNativeThreadState
private external fun _nGetCanvas(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeSurfaceI")
// Needs ForceNativeThreadState: creates a GPU surface or allocates and zero-fills a pixel buffer whose size grows with
// the surface dimensions.
// Native call path: SkSurface::makeSurface -> SkSurface_Ganesh::onNewSurface -> SkSurfaces::RenderTarget
@ForceNativeThreadState
private external fun _nMakeSurfaceI(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeSurface")
// Needs ForceNativeThreadState: creates a GPU surface or allocates and zero-fills a pixel buffer whose size grows with
// the surface dimensions.
// Native call path: SkSurface::makeSurface -> SkSurface_Ganesh::onNewSurface -> SkSurfaces::RenderTarget
@ForceNativeThreadState
private external fun _nMakeSurface(ptr: NativePointer, width: Int, height: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeImageSnapshot")
// Needs ForceNativeThreadState: copies the surface into a new image, either as a GPU copy or a pixel copy whose time
// grows with the surface size.
// Native call path: SkSurface::makeImageSnapshot -> onNewImageSnapshot
@ForceNativeThreadState
private external fun _nMakeImageSnapshot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nMakeImageSnapshotR")
// Needs ForceNativeThreadState: copies part of the surface into a new image, either as a GPU copy or a pixel read whose
// time grows with the copied area.
// Native call path: SkSurface::makeImageSnapshot -> onNewImageSnapshot
@ForceNativeThreadState
private external fun _nMakeImageSnapshotR(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Surface__1nDraw")
// Needs ForceNativeThreadState: draws an image onto a canvas that may use the GPU.
// Native call path: SkSurface::draw -> onDraw -> SkCanvas::drawImage
@ForceNativeThreadState
private external fun _nDraw(ptr: NativePointer, canvasPtr: NativePointer, x: Float, y: Float, samplingModeValue1: Int, samplingModeValue2: Int, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nPeekPixels")
// Needs ForceNativeThreadState: with capture enabled, may grow a list that moves every tracked canvas, so its time
// grows with the canvas count.
// Native call path: SkSurface::peekPixels -> SkSurface::getCanvas -> SkCaptureManager::makeCaptureCanvas
@ForceNativeThreadState
private external fun _nPeekPixels(ptr: NativePointer, pixmapPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Surface__1nReadPixelsToPixmap")
// Needs ForceNativeThreadState: reads pixels back from the GPU through Metal and waits for the GPU; for CPU surfaces
// its time grows with the surface size.
// Native call path: SkSurface::readPixels -> Device::onReadPixels -> SurfaceContext::readPixels ->
//   GrMtlGpu::onReadPixels
@ForceNativeThreadState
private external fun _nReadPixelsToPixmap(ptr: NativePointer, pixmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Surface__1nWritePixelsFromPixmap")
// Needs ForceNativeThreadState: uploads pixels to the GPU through Metal, or copies pixels on a CPU surface, so its time
// grows with the pixel count.
// Native call path: SkSurface::writePixels -> Device::onWritePixels -> SurfaceContext::writePixels ->
//   GrMtlGpu::onWritePixels
@ForceNativeThreadState
private external fun _nWritePixelsFromPixmap(ptr: NativePointer, pixmapPtr: NativePointer, x: Int, y: Int)

@ExternalSymbolName("org_jetbrains_skia_Surface__1nUnique")
// No ForceNativeThreadState: reads a reference count with one atomic load, so it is short and cannot block.
// Native call path: SkRefCnt::unique
private external fun _nUnique(ptr: NativePointer): Boolean
