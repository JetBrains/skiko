package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import kotlin.math.min

class Bitmap internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR), IHasImageInfo {
    companion object {
        fun makeFromImage(image: Image): Bitmap {
            val bitmap = Bitmap()
            bitmap.allocPixels(image.imageInfo)
            return if (image.readPixels(bitmap)) bitmap else {
                bitmap.close()
                throw RuntimeException("Failed to readPixels from $image")
            }
        }

        fun makeFromImage(image: Image, context: DirectContext): Bitmap {
            val bitmap = Bitmap()
            bitmap.allocPixels(image.imageInfo)
            return if (image.readPixels(context, bitmap)) bitmap else {
                bitmap.close()
                throw RuntimeException("Failed to readPixels from $image")
            }
        }

        init {
            staticLoad()
        }
    }

    internal var _imageInfo: ImageInfo? = null

    /**
     * Creates an empty Bitmap without pixels, with [ColorType.UNKNOWN],
     * [ColorAlphaType.UNKNOWN], and with a width and height of zero.
     * PixelRef origin is set to (0, 0). Bitmap is not volatile.
     *
     * Use [.setImageInfo] to associate ColorType, ColorAlphaType, width, and height after Bitmap has been created.
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_empty_constructor](https://fiddle.skia.org/c/@Bitmap_empty_constructor)
     */
    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }

    /**
     * Copies settings from src to returned Bitmap. Shares pixels if src has pixels
     * allocated, so both bitmaps reference the same pixels.
     *
     * @return  copy of src
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_copy_const_SkBitmap](https://fiddle.skia.org/c/@Bitmap_copy_const_SkBitmap)
     */
    fun makeClone(): Bitmap {
        return try {
            Stats.onNativeCall()
            Bitmap(_nMakeClone(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Swaps the fields of the two bitmaps.
     *
     * @param other  Bitmap exchanged with original
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_swap](https://fiddle.skia.org/c/@Bitmap_swap)
     */
    fun swap(other: Bitmap) {
        Stats.onNativeCall()
        try {
            _nSwap(_ptr, getPtr(other))
            _imageInfo = null
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    override val imageInfo: ImageInfo
        get() = try {
            if (_imageInfo == null) {
                _imageInfo = ImageInfo.createUsing(
                    _ptr = _ptr,
                    _nGetImageInfo = ::_nGetImageInfo
                )
            }
            _imageInfo!!
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns number of pixels that fit on row. Should be greater than or equal to
     * getWidth().
     *
     * @return  maximum pixels per row
     */
    val rowBytesAsPixels: Int
        get() = try {
            Stats.onNativeCall()
            _nGetRowBytesAsPixels(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns true if PixelRef is null.
     *
     * Does not check if width or height are zero; call [.drawsNothing]
     * to check width, height, and PixelRef.
     *
     * @return  true if no PixelRef is associated
     */
    val isNull: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsNull(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Returns true if width or height are zero, or if PixelRef is null.
     * If true, Bitmap has no effect when drawn or drawn into.
     *
     * @return  true if drawing has no effect
     */
    fun drawsNothing(): Boolean {
        return isEmpty || isNull
    }

    /**
     * Returns row bytes, the interval from one pixel row to the next. Row bytes
     * is at least as large as: getWidth() * getBytesPerPixel().
     *
     * Returns zero if getColorType() is [ColorType.UNKNOWN], or if row bytes
     * supplied to [.setImageInfo] is not large enough to hold a row of pixels.
     *
     * @return  byte length of pixel row
     */
    val rowBytes: Int
        get() = try {
            Stats.onNativeCall()
            _nGetRowBytes(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Sets alpha type, if argument is compatible with current color type.
     * Returns true unless argument is [ColorAlphaType.UNKNOWN] and current
     * value is [ColorAlphaType.UNKNOWN].
     *
     *
     * Returns true if current color type is [ColorType.UNKNOWN].
     * Argument is ignored, and alpha type remains [ColorAlphaType.UNKNOWN].
     *
     *
     * Returns true if current color type is [ColorType.RGB_565] or
     * [ColorType.GRAY_8]. Argument is ignored, and alpha type remains
     * [ColorAlphaType.OPAQUE].
     *
     *
     * If current color type is [ColorType.ARGB_4444], [ColorType.RGBA_8888],
     * [ColorType.BGRA_8888], or [ColorType.RGBA_F16]: returns true unless
     * argument is [ColorAlphaType.UNKNOWN] and current alpha type is not
     * [ColorAlphaType.UNKNOWN]. If current alpha type is
     * [ColorAlphaType.UNKNOWN], argument is ignored.
     *
     *
     * If current color type is [ColorType.ALPHA_8], returns true unless
     * argument is [ColorAlphaType.UNKNOWN] and current alpha type is not
     * [ColorAlphaType.UNKNOWN]. If current alpha type is
     * [ColorAlphaType.UNKNOWN], argument is ignored. If argument is
     * [ColorAlphaType.UNPREMUL], it is treated as [ColorAlphaType.PREMUL].
     *
     *
     * This changes alpha type in PixelRef; all bitmaps sharing PixelRef
     * are affected.
     *
     * @return  true if alpha type is set
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_setAlphaType](https://fiddle.skia.org/c/@Bitmap_setAlphaType)
     */
    fun setAlphaType(alphaType: ColorAlphaType): Boolean {
        return try {
            Stats.onNativeCall()
            _imageInfo = null
            _nSetAlphaType(_ptr, alphaType.ordinal)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns minimum memory required for pixel storage.
     * Does not include unused memory on last row when getRowBytesAsPixels() exceeds getWidth().
     * Returns zero if height() or width() is 0.
     * Returns getHeight() times getRowBytes() if getColorType() is [ColorType.UNKNOWN].
     *
     * @return  size in bytes of image buffer
     */
    fun computeByteSize(): Int {
        return try {
            Stats.onNativeCall()
            _nComputeByteSize(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Returns true if pixels can not change.
     *
     *
     * Most immutable Bitmap checks trigger an assert only on debug builds.
     *
     * @return  true if pixels are immutable
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_isImmutable](https://fiddle.skia.org/c/@Bitmap_isImmutable)
     */
    val isImmutable: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsImmutable(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Sets internal flag to mark Bitmap as immutable. Once set, pixels can not change.
     * Any other bitmap sharing the same PixelRef are also marked as immutable.
     * Once PixelRef is marked immutable, the setting cannot be cleared.
     *
     *
     * Writing to immutable Bitmap pixels triggers an assert on debug builds.
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_setImmutable](https://fiddle.skia.org/c/@Bitmap_setImmutable)
     */
    fun setImmutable(): Bitmap {
        Stats.onNativeCall()
        _nSetImmutable(_ptr)
        return this
    }

    /**
     *
     * Resets to its initial state; all fields are set to zero, as if Bitmap had
     * been initialized by Bitmap().
     *
     *
     * Sets width, height, row bytes to zero; pixel address to nullptr; ColorType to
     * [ColorType.UNKNOWN]; and ColorAlphaType to [ColorAlphaType.UNKNOWN].
     *
     *
     * If PixelRef is allocated, its reference count is decreased by one, releasing
     * its memory if Bitmap is the sole owner.
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_reset](https://fiddle.skia.org/c/@Bitmap_reset)
     */
    fun reset(): Bitmap {
        Stats.onNativeCall()
        _imageInfo = null
        _nReset(_ptr)
        return this
    }

    /**
     *
     * Returns true if all pixels are opaque. ColorType determines how pixels
     * are encoded, and whether pixel describes alpha. Returns true for ColorType
     * without alpha in each pixel; for other ColorType, returns true if all
     * pixels have alpha values equivalent to 1.0 or greater.
     *
     *
     * For [ColorType.RGB_565] or [ColorType.GRAY_8]: always
     * returns true. For [ColorType.ALPHA_8], [ColorType.BGRA_8888],
     * [ColorType.RGBA_8888]: returns true if all pixel alpha values are 255.
     * For [ColorType.ARGB_4444]: returns true if all pixel alpha values are 15.
     * For [ColorType.RGBA_F16]: returns true if all pixel alpha values are 1.0 or
     * greater.
     *
     *
     * Returns false for [ColorType.UNKNOWN].
     *
     * @return    true if all pixels have opaque values or ColorType is opaque
     */
    fun computeIsOpaque(): Boolean {
        return try {
            Stats.onNativeCall()
            _nComputeIsOpaque(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns IRect { 0, 0, width(), height() }.
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_getBounds_2](https://fiddle.skia.org/c/@Bitmap_getBounds_2)
     */
    val bounds: IRect
        get() = IRect.makeXYWH(0, 0, width, height)

    /**
     * Returns the bounds of this bitmap, offset by its PixelRef origin.
     */
    val subset: IRect
        get() {
            val origin = pixelRefOrigin
            return IRect.makeXYWH(origin.x, origin.y, width, height)
        }

    /**
     *
     * Sets width, height, ColorAlphaType, ColorType, ColorSpace.
     * Frees pixels, and returns true if successful.
     *
     *
     * imageInfo.getAlphaType() may be altered to a value permitted by imageInfo.getColorSpace().
     * If imageInfo.getColorType() is [ColorType.UNKNOWN], imageInfo.getAlphaType() is
     * set to [ColorAlphaType.UNKNOWN].
     * If imageInfo.colorType() is [ColorType.ALPHA_8] and imageInfo.getAlphaType() is
     * [ColorAlphaType.UNPREMUL], imageInfo.getAlphaType() is replaced by [ColorAlphaType.PREMUL].
     * If imageInfo.colorType() is [ColorType.RGB_565] or [ColorType.GRAY_8],
     * imageInfo.getAlphaType() is set to [ColorAlphaType.OPAQUE].
     * If imageInfo.colorType() is [ColorType.ARGB_4444], [ColorType.RGBA_8888],
     * [ColorType.BGRA_8888], or [ColorType.RGBA_F16]: imageInfo.getAlphaType() remains
     * unchanged.
     *
     *
     * Calls reset() and returns false if:
     * - rowBytes exceeds 31 bits
     * - imageInfo.getWidth() is negative
     * - imageInfo.getHeight() is negative
     * - rowBytes is positive and less than imageInfo.getWidth() times imageInfo.getBytesPerPixel()
     *
     * @param imageInfo  contains width, height, AlphaType, ColorType, ColorSpace
     * @return           true if ImageInfo set successfully
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_setInfo](https://fiddle.skia.org/c/@Bitmap_setInfo)
     */
    fun setImageInfo(imageInfo: ImageInfo): Boolean {
        _imageInfo = null
        return setImageInfo(imageInfo, 0)
    }

    /**
     *
     * Sets width, height, ColorAlphaType, ColorType, ColorSpace, and optional
     * rowBytes. Frees pixels, and returns true if successful.
     *
     *
     * imageInfo.getAlphaType() may be altered to a value permitted by imageInfo.getColorSpace().
     * If imageInfo.getColorType() is [ColorType.UNKNOWN], imageInfo.getAlphaType() is
     * set to [ColorAlphaType.UNKNOWN].
     * If imageInfo.colorType() is [ColorType.ALPHA_8] and imageInfo.getAlphaType() is
     * [ColorAlphaType.UNPREMUL], imageInfo.getAlphaType() is replaced by [ColorAlphaType.PREMUL].
     * If imageInfo.colorType() is [ColorType.RGB_565] or [ColorType.GRAY_8],
     * imageInfo.getAlphaType() is set to [ColorAlphaType.OPAQUE].
     * If imageInfo.colorType() is [ColorType.ARGB_4444], [ColorType.RGBA_8888],
     * [ColorType.BGRA_8888], or [ColorType.RGBA_F16]: imageInfo.getAlphaType() remains
     * unchanged.
     *
     *
     * rowBytes must equal or exceed imageInfo.getMinRowBytes(). If imageInfo.getColorSpace() is
     * [ColorType.UNKNOWN], rowBytes is ignored and treated as zero; for all other
     * ColorSpace values, rowBytes of zero is treated as imageInfo.getMinRowBytes().
     *
     *
     * Calls reset() and returns false if:
     * - rowBytes exceeds 31 bits
     * - imageInfo.getWidth() is negative
     * - imageInfo.getHeight() is negative
     * - rowBytes is positive and less than imageInfo.getWidth() times imageInfo.getBytesPerPixel()
     *
     * @param imageInfo  contains width, height, AlphaType, ColorType, ColorSpace
     * @param rowBytes   imageInfo.getMinRowBytes() or larger; or zero
     * @return           true if ImageInfo set successfully
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_setInfo](https://fiddle.skia.org/c/@Bitmap_setInfo)
     */
    fun setImageInfo(imageInfo: ImageInfo, rowBytes: Int): Boolean {
        return try {
            _imageInfo = null
            Stats.onNativeCall()
            _nSetImageInfo(
                _ptr,
                imageInfo.width,
                imageInfo.height,
                imageInfo.colorInfo.colorType.ordinal,
                imageInfo.colorInfo.alphaType.ordinal,
                getPtr(imageInfo.colorInfo.colorSpace),
                rowBytes
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(imageInfo.colorInfo.colorSpace)
        }
    }

    /**
     *
     * Sets ImageInfo to info following the rules in setImageInfo() and allocates pixel
     * memory. Memory is zeroed.
     *
     *
     * Returns false and calls reset() if ImageInfo could not be set, or memory could
     * not be allocated, or memory could not optionally be zeroed.
     *
     *
     * On most platforms, allocating pixel memory may succeed even though there is
     * not sufficient memory to hold pixels; allocation does not take place
     * until the pixels are written to. The actual behavior depends on the platform
     * implementation of calloc().
     *
     * @param imageInfo   contains width, height, ColorAlphaType, ColorType, ColorSpace
     * @param zeroPixels  whether pixels should be zeroed
     * @return            true if pixels allocation is successful
     */
    fun allocPixelsFlags(imageInfo: ImageInfo, zeroPixels: Boolean): Boolean {
        return try {
            _imageInfo = null
            Stats.onNativeCall()
            _nAllocPixelsFlags(
                _ptr,
                imageInfo.width,
                imageInfo.height,
                imageInfo.colorInfo.colorType.ordinal,
                imageInfo.colorInfo.alphaType.ordinal,
                getPtr(imageInfo.colorInfo.colorSpace),
                if (zeroPixels) 1 else 0
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(imageInfo.colorInfo.colorSpace)
        }
    }

    /**
     *
     * Sets ImageInfo to info following the rules in setImageInfo() and allocates pixel
     * memory. rowBytes must equal or exceed info.width() times info.bytesPerPixel(),
     * or equal zero.
     *
     *
     * Returns false and calls reset() if ImageInfo could not be set, or memory could
     * not be allocated.
     *
     *
     * On most platforms, allocating pixel memory may succeed even though there is
     * not sufficient memory to hold pixels; allocation does not take place
     * until the pixels are written to. The actual behavior depends on the platform
     * implementation of malloc().
     *
     * @param info      contains width, height, ColorAlphaType, ColorType, ColorSpace
     * @param rowBytes  size of pixel row or larger; may be zero
     * @return          true if pixel storage is allocated
     */
    fun allocPixels(info: ImageInfo, rowBytes: Int): Boolean {
        return try {
            _imageInfo = null
            Stats.onNativeCall()
            _nAllocPixelsRowBytes(
                _ptr,
                info.width,
                info.height,
                info.colorInfo.colorType.ordinal,
                info.colorInfo.alphaType.ordinal,
                getPtr(info.colorInfo.colorSpace),
                rowBytes
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(info.colorInfo.colorSpace)
        }
    }

    /**
     *
     * Sets ImageInfo to info following the rules in [.setImageInfo] and allocates pixel
     * memory.
     *
     *
     * Returns false and calls reset() if ImageInfo could not be set, or memory could
     * not be allocated.
     *
     *
     * On most platforms, allocating pixel memory may succeed even though there is
     * not sufficient memory to hold pixels; allocation does not take place
     * until the pixels are written to. The actual behavior depends on the platform
     * implementation of malloc().
     *
     * @param imageInfo  contains width, height, ColorAlphaType, ColorType, ColorSpace
     * @return           true if pixel storage is allocated
     * @see [https://fiddle.skia.org/c/@Bitmap_allocPixels_2](https://fiddle.skia.org/c/@Bitmap_allocPixels_2)
     */
    fun allocPixels(imageInfo: ImageInfo): Boolean {
        return allocPixels(imageInfo, imageInfo.minRowBytes)
    }
    /**
     * Sets ImageInfo to width, height, and native color type; and allocates
     * pixel memory. If opaque is true, sets ImageInfo to [ColorAlphaType.OPAQUE];
     * otherwise, sets to [ColorAlphaType.PREMUL].
     *
     * Calls reset() and returns false if width exceeds 29 bits or is negative,
     * or height is negative.
     *
     * Returns false if allocation fails.
     *
     * Use to create Bitmap that matches the native pixel arrangement on
     * the platform. Bitmap drawn to output device skips converting its pixel format.
     *
     * @param width   pixel column count; must be zero or greater
     * @param height  pixel row count; must be zero or greater
     * @param opaque  true if pixels do not have transparency
     * @return        true if pixel storage is allocated
     */
    /**
     * Sets ImageInfo to width, height, and native color type; and allocates
     * pixel memory. Sets ImageInfo to [ColorAlphaType.PREMUL].
     *
     * Calls reset() and returns false if width exceeds 29 bits or is negative,
     * or height is negative.
     *
     * Returns false if allocation fails.
     *
     * Use to create Bitmap that matches the native pixel arrangement on
     * the platform. Bitmap drawn to output device skips converting its pixel format.
     *
     * @param width   pixel column count; must be zero or greater
     * @param height  pixel row count; must be zero or greater
     * @return        true if pixel storage is allocated
     */
    fun allocN32Pixels(width: Int, height: Int, opaque: Boolean = false): Boolean {
        return allocPixels(
            ImageInfo.makeN32(
                width,
                height,
                if (opaque) ColorAlphaType.OPAQUE else ColorAlphaType.PREMUL
            )
        )
    }

    fun installPixels(pixels: ByteArray?): Boolean {
        return installPixels(imageInfo, pixels, rowBytes)
    }

    /**
     *
     * Sets ImageInfo to info following the rules in setImageInfo(), and creates PixelRef
     * containing pixels and rowBytes.
     *
     *
     * If ImageInfo could not be set, or rowBytes is less than info.getMinRowBytes():
     * calls reset(), and returns false.
     *
     * @param info     contains width, height, SkAlphaType, SkColorType, SkColorSpace
     * @param pixels   pixel storage
     * @param rowBytes size of pixel row or larger
     * @return         true if ImageInfo is set to info
     */
    fun installPixels(
        info: ImageInfo,
        pixels: ByteArray?,
        rowBytes: Int
    ): Boolean {
        return try {
            _imageInfo = null
            Stats.onNativeCall()
            interopScope {
                _nInstallPixels(
                    _ptr,
                    info.width,
                    info.height,
                    info.colorInfo.colorType.ordinal,
                    info.colorInfo.alphaType.ordinal,
                    getPtr(info.colorInfo.colorSpace),
                    toInterop(pixels),
                    rowBytes,
                    pixels?.size ?: 0
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(info.colorInfo.colorSpace)
        }
    }

    /**
     *
     * Allocates pixel memory with HeapAllocator, and replaces existing PixelRef.
     * The allocation size is determined by ImageInfo width, height, and ColorType.
     *
     *
     * Returns false if info().colorType() is [ColorType.UNKNOWN], or allocation fails.
     *
     * @return  true if the allocation succeeds
     */
    fun allocPixels(): Boolean {
        return try {
            _imageInfo = null
            Stats.onNativeCall()
            _nAllocPixels(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns PixelRef, which contains: pixel base address; its dimensions; and
     * rowBytes(), the interval from one row to the next.
     * PixelRef may be shared by multiple bitmaps.
     * If PixelRef has not been set, returns null.
     *
     * @return  SkPixelRef, or null
     */
    val pixelRef: PixelRef?
        get() = try {
            Stats.onNativeCall()
            val res = _nGetPixelRef(_ptr)
            if (res == NullPointer) null else PixelRef(res)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Returns origin of pixels within PixelRef. Bitmap bounds is always contained
     * by PixelRef bounds, which may be the same size or larger. Multiple Bitmap
     * can share the same PixelRef, where each Bitmap has different bounds.
     *
     *
     * The returned origin added to Bitmap dimensions equals or is smaller than the
     * PixelRef dimensions.
     *
     *
     * Returns (0, 0) if PixelRef is nullptr.
     *
     * @return  pixel origin within PixelRef
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_pixelRefOrigin](https://fiddle.skia.org/c/@Bitmap_pixelRefOrigin)
     */
    val pixelRefOrigin: IPoint
        get() = try {
            Stats.onNativeCall()
            val resX = _nGetPixelRefOriginX(_ptr)
            val resY = _nGetPixelRefOriginY(_ptr)
            IPoint(resX, resY)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Replaces pixelRef and origin in Bitmap. dx and dy specify the offset
     * within the PixelRef pixels for the top-left corner of the bitmap.
     *
     *
     * Asserts in debug builds if dx or dy are out of range. Pins dx and dy
     * to legal range in release builds.
     *
     *
     * The caller is responsible for ensuring that the pixels match the
     * ColorType and ColorAlphaType in ImageInfo.
     *
     * @param pixelRef  PixelRef describing pixel address and rowBytes()
     * @param dx        column offset in PixelRef for bitmap origin
     * @param dy        row offset in PixelRef for bitmap origin
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_setPixelRef](https://fiddle.skia.org/c/@Bitmap_setPixelRef)
     */
    fun setPixelRef(pixelRef: PixelRef?, dx: Int, dy: Int): Bitmap {
        return try {
            _imageInfo = null
            Stats.onNativeCall()
            _nSetPixelRef(
                _ptr,
                getPtr(pixelRef),
                dx,
                dy
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pixelRef)
        }
    }

    /**
     * Returns true if Bitmap can be drawn.
     *
     * @return  true if getPixels() is not null
     */
    val isReadyToDraw: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsReadyToDraw(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Returns a unique value corresponding to the pixels in PixelRef.
     * Returns a different value after notifyPixelsChanged() has been called.
     * Returns zero if PixelRef is null.
     *
     *
     * Determines if pixels have changed since last examined.
     *
     * @return  unique value for pixels in PixelRef
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_getGenerationID](https://fiddle.skia.org/c/@Bitmap_getGenerationID)
     */
    val generationId: Int
        get() = try {
            Stats.onNativeCall()
            _nGetGenerationId(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Marks that pixels in PixelRef have changed. Subsequent calls to
     * getGenerationId() return a different value.
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_notifyPixelsChanged](https://fiddle.skia.org/c/@Bitmap_notifyPixelsChanged)
     */
    fun notifyPixelsChanged(): Bitmap {
        return try {
            Stats.onNativeCall()
            _nNotifyPixelsChanged(_ptr)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Replaces pixel values with color, interpreted as being in the sRGB ColorSpace.
     * All pixels contained by getBounds() are affected. If the getColorType() is
     * [ColorType.GRAY_8] or [ColorType.RGB_565], then alpha is ignored; RGB is
     * treated as opaque. If getColorType() is [ColorType.ALPHA_8], then RGB is ignored.
     *
     * @param color  unpremultiplied color
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_eraseColor](https://fiddle.skia.org/c/@Bitmap_eraseColor)
     */
    fun erase(color: Int): Bitmap {
        return try {
            Stats.onNativeCall()
            _nEraseColor(_ptr, color)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Replaces pixel values inside area with color, interpreted as being in the sRGB
     * ColorSpace. If area does not intersect getBounds(), call has no effect.
     *
     * If the getColorType() is [ColorType.GRAY_8] or [ColorType.RGB_565],
     * then alpha is ignored; RGB is treated as opaque. If getColorType() is
     * [ColorType.ALPHA_8], then RGB is ignored.
     *
     * @param color  unpremultiplied color
     * @param area   rectangle to fill
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_erase](https://fiddle.skia.org/c/@Bitmap_erase)
     */
    fun erase(color: Int, area: IRect): Bitmap {
        return try {
            Stats.onNativeCall()
            _nErase(_ptr, color, area.left, area.top, area.right, area.bottom)
            this
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Returns pixel at (x, y) as unpremultiplied color.
     * Returns black with alpha if ColorType is [ColorType.ALPHA_8].
     *
     *
     * Input is not validated: out of bounds values of x or y returns undefined values
     * or may crash if. Fails if ColorType is [ColorType.UNKNOWN] or
     * pixel address is nullptr.
     *
     *
     * ColorSpace in ImageInfo is ignored. Some color precision may be lost in the
     * conversion to unpremultiplied color; original pixel data may have additional
     * precision.
     *
     * @param x  column index, zero or greater, and less than getWidth()
     * @param y  row index, zero or greater, and less than getHeight()
     * @return   pixel converted to unpremultiplied color
     */
    fun getColor(x: Int, y: Int): Int {
        return try {
            Stats.onNativeCall()
            Bitmap_nGetColor(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Look up the pixel at (x,y) and return its alpha component, normalized to [0..1].
     * This is roughly equivalent to GetColorA(getColor()), but can be more efficent
     * (and more precise if the pixels store more than 8 bits per component).
     *
     * @param x  column index, zero or greater, and less than getWidth()
     * @param y  row index, zero or greater, and less than getHeight()
     * @return   alpha converted to normalized float
     */
    fun getAlphaf(x: Int, y: Int): Float {
        return try {
            Stats.onNativeCall()
            _nGetAlphaf(_ptr, x, y)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Shares PixelRef with dst. Pixels are not copied; this and dst point
     * to the same pixels; dst.getBounds() are set to the intersection of subset
     * and the original getBounds().
     *
     *
     * subset may be larger than getBounds(). Any area outside of getBounds() is ignored.
     *
     *
     * Any contents of dst are discarded. isVolatile() setting is copied to dst.
     * dst is set to getColorType(), getAlphaType(), and getColorSpace().
     *
     *
     * Return false if:
     *
     *  * dst is null
     *  * PixelRef is null
     *  * subset does not intersect getBounds()
     *
     *
     * @param dst     Bitmap set to subset
     * @param subset  rectangle of pixels to reference
     * @return        true if dst is replaced by subset
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_extractSubset](https://fiddle.skia.org/c/@Bitmap_extractSubset)
     */
    fun extractSubset(dst: Bitmap, subset: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nExtractSubset(
                _ptr,
                getPtr(dst),
                subset.left,
                subset.top,
                subset.right,
                subset.bottom
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dst)
        }
    }

    /**
     *
     * Copies a rect of pixels from Bitmap. Copy starts at (srcX, srcY),
     * and does not exceed Bitmap (getWidth(), getHeight()).
     *
     *
     * dstInfo specifies width, height, ColorType, AlphaType, and ColorSpace of
     * destination. dstRowBytes specifics the gap from one destination row to the next.
     * Returns true if pixels are copied. Returns false if:
     *
     *
     *  * dstRowBytes is less than dstInfo.getMinRowBytes()
     *  * PixelRef is null
     *
     *
     *
     * Pixels are copied only if pixel conversion is possible. If Bitmap getColorType() is
     * [ColorType.GRAY_8], or [ColorType.ALPHA_8]; dstInfo.colorType() must match.
     * If Bitmap getClorType() is [ColorType.GRAY_8], dstInfo.getColorSpace() must match.
     * If Bitmap getAlphaType() is [ColorAlphaType.OPAQUE], dstInfo.getAlphaType() must
     * match. If Bitmap getColorSpace() is null, dstInfo.getColorSpace() must match. Returns
     * false if pixel conversion is not possible.
     *
     *
     * srcX and srcY may be negative to copy only top or left of source. Returns
     * false if getWidth() or getHeight() is zero or negative.
     * Returns false if abs(srcX) &gt;= getWidth(), or if abs(srcY) &gt;= getHeight().
     *
     * @param dstInfo      destination width, height, ColorType, AlphaType, ColorSpace
     * @param dstRowBytes  destination row length
     * @param srcX         column index whose absolute value is less than width()
     * @param srcY         row index whose absolute value is less than height()
     * @return             pixel data or null
     */
    fun readPixels(
        dstInfo: ImageInfo = imageInfo,
        dstRowBytes: Int = rowBytes,
        srcX: Int = 0,
        srcY: Int = 0
    ): ByteArray? {
        val size = getReadPixelsArraySize(dstInfo, dstRowBytes, srcY)
        val bitmapPixels = ByteArray(size)
        val successfulRead = readPixels(bitmapPixels, dstInfo, dstRowBytes, srcX, srcY)
        return bitmapPixels.takeIf { successfulRead }
    }

    /**
     * See documentation for [readPixels]
     *
     * @param byteArray array where pixels will be read.
     */
    internal fun readPixels(
        byteArray: ByteArray,
        dstInfo: ImageInfo = imageInfo,
        dstRowBytes: Int = rowBytes,
        srcX: Int = 0,
        srcY: Int = 0
    ): Boolean {
        check(byteArray.size == getReadPixelsArraySize(dstInfo, dstRowBytes, srcY)) {
            "byteArray is not properly allocated. Use readPixelsArraySize"
        }
        try {
            Stats.onNativeCall()
            interopScope {
                val byteArrayHandle = toInteropForResult(byteArray)
                val successfulRead = _nReadPixels(
                    _ptr,
                    dstInfo.width,
                    dstInfo.height,
                    dstInfo.colorInfo.colorType.ordinal,
                    dstInfo.colorInfo.alphaType.ordinal,
                    getPtr(dstInfo.colorInfo.colorSpace),
                    dstRowBytes,
                    srcX,
                    srcY,
                    byteArrayHandle
                )
                if (successfulRead) {
                    byteArrayHandle.fromInterop(byteArray)
                }
                return successfulRead
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dstInfo.colorInfo.colorSpace)
        }
    }

    internal fun getReadPixelsArraySize(
        dstInfo: ImageInfo = imageInfo,
        dstRowBytes: Int = rowBytes,
        srcY: Int = 0
    ): Int = min(dstInfo.height, height - srcY) * dstRowBytes

    /**
     *
     * Sets dst to alpha described by pixels. Returns false if dst cannot
     * be written to or dst pixels cannot be allocated.
     *
     * @param dst holds PixelRef to fill with alpha layer
     * @return    true if alpha layer was not constructed in dst PixelRef
     */
    fun extractAlpha(dst: Bitmap): Boolean {
        return extractAlpha(dst, null) != null
    }

    /**
     *
     * Sets dst to alpha described by pixels. Returns false if dst cannot
     * be written to or dst pixels cannot be allocated.
     *
     *
     * If paint is not null and contains MaskFilter, MaskFilter
     * generates mask alpha from Bitmap. Returns offset to top-left position for dst
     * for alignment with Bitmap; (0, 0) unless MaskFilter generates mask.
     *
     * @param dst   holds PixelRef to fill with alpha layer
     * @param paint holds optional MaskFilter; may be null
     * @return      null if alpha layer was not constructed in dst PixelRef, IPoint otherwise
     */
    fun extractAlpha(dst: Bitmap, paint: Paint?): IPoint? {
        return try {
            Stats.onNativeCall()
            withNullableResult(IntArray(2)) {
                _nExtractAlpha(
                    ptr = _ptr,
                    dstPtr = getPtr(dst),
                    paintPtr = getPtr(paint),
                    iPointResultIntArray = it
                )
            }?.let {
                IPoint(x = it[0], y = it[1])
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dst)
            reachabilityBarrier(paint)
        }
    }

    /**
     * Create a pixmap and copy buffer contents into it
     *
     * @return Pixamp with copied pixels, or null
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_peekPixels](https://fiddle.skia.org/c/@Bitmap_peekPixels)
     */
    fun peekPixels(): Pixmap? {
        return try {
            Stats.onNativeCall()
            val res = _nPeekPixels(_ptr)
            if (res == NullPointer) {
                null
            } else {
                Pixmap(res, true)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun makeShader(localMatrix: Matrix33?): Shader {
        return makeShader(FilterTileMode.CLAMP, FilterTileMode.CLAMP, SamplingMode.DEFAULT, localMatrix)
    }

    fun makeShader(
        tmx: FilterTileMode,
        tmy: FilterTileMode,
        localMatrix: Matrix33?
    ): Shader {
        return makeShader(tmx, tmy, SamplingMode.DEFAULT, localMatrix)
    }

    fun makeShader(
        tmx: FilterTileMode = FilterTileMode.CLAMP,
        tmy: FilterTileMode = FilterTileMode.CLAMP,
        sampling: SamplingMode = SamplingMode.DEFAULT,
        localMatrix: Matrix33? = null
    ): Shader {
        return try {
            Stats.onNativeCall()
            Shader(
                interopScope {
                    _nMakeShader(
                        _ptr,
                        tmx.ordinal,
                        tmy.ordinal,
                        sampling._packedInt1(),
                        sampling._packedInt2(),
                        toInterop(localMatrix?.mat)
                    )
                }
            )
        } finally {
            reachabilityBarrier(this)
        }
    }

    private object _FinalizerHolder {
        val PTR = Bitmap_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetFinalizer")
private external fun Bitmap_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nMake")
private external fun _nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nMakeClone")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nMakeClone")
private external fun _nMakeClone(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSwap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nSwap")
private external fun _nSwap(ptr: NativePointer, otherPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetPixmap")
private external fun _nGetPixmap(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetImageInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetImageInfo")
private external fun _nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetRowBytesAsPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetRowBytesAsPixels")
private external fun _nGetRowBytesAsPixels(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsNull")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nIsNull")
private external fun _nIsNull(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetRowBytes")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetRowBytes")
private external fun _nGetRowBytes(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetAlphaType")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nSetAlphaType")
private external fun _nSetAlphaType(ptr: NativePointer, alphaType: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nComputeByteSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nComputeByteSize")
private external fun _nComputeByteSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsImmutable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nIsImmutable")
private external fun _nIsImmutable(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetImmutable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nSetImmutable")
private external fun _nSetImmutable(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsVolatile")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nIsVolatile")
private external fun _nIsVolatile(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetVolatile")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nSetVolatile")
private external fun _nSetVolatile(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nReset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nReset")
private external fun _nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nComputeIsOpaque")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nComputeIsOpaque")
private external fun _nComputeIsOpaque(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetImageInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nSetImageInfo")
private external fun _nSetImageInfo(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    rowBytes: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nAllocPixelsFlags")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nAllocPixelsFlags")
private external fun _nAllocPixelsFlags(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    flags: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nAllocPixelsRowBytes")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nAllocPixelsRowBytes")
private external fun _nAllocPixelsRowBytes(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    rowBytes: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nInstallPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nInstallPixels")
private external fun _nInstallPixels(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixels: InteropPointer,
    rowBytes: Int,
    pixelsLen: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nAllocPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nAllocPixels")
private external fun _nAllocPixels(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixelRef")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetPixelRef")
private external fun _nGetPixelRef(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixelRefOriginX")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetPixelRefOriginX")
private external fun _nGetPixelRefOriginX(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixelRefOriginY")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetPixelRefOriginY")
private external fun _nGetPixelRefOriginY(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetPixelRef")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nSetPixelRef")
private external fun _nSetPixelRef(ptr: NativePointer, pixelRefPtr: NativePointer, dx: Int, dy: Int)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsReadyToDraw")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nIsReadyToDraw")
private external fun _nIsReadyToDraw(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetGenerationId")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetGenerationId")
private external fun _nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nNotifyPixelsChanged")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nNotifyPixelsChanged")
private external fun _nNotifyPixelsChanged(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nEraseColor")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nEraseColor")
private external fun _nEraseColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nErase")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nErase")
private external fun _nErase(ptr: NativePointer, color: Int, left: Int, top: Int, right: Int, bottom: Int)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetColor")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetColor")
private external fun Bitmap_nGetColor(ptr: NativePointer, x: Int, y: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetAlphaf")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nGetAlphaf")
private external fun _nGetAlphaf(ptr: NativePointer, x: Int, y: Int): Float

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nExtractSubset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nExtractSubset")
private external fun _nExtractSubset(ptr: NativePointer, dstPtr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nReadPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nReadPixels")
private external fun _nReadPixels(
    ptr: NativePointer,
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    dstRowBytes: Int,
    srcX: Int,
    srcY: Int,
    resultBytes: InteropPointer
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nExtractAlpha")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nExtractAlpha")
private external fun _nExtractAlpha(ptr: NativePointer, dstPtr: NativePointer, paintPtr: NativePointer, iPointResultIntArray: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nPeekPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nPeekPixels")
private external fun _nPeekPixels(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nMakeShader")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Bitmap__1nMakeShader")
private external fun _nMakeShader(ptr: NativePointer, tmx: Int, tmy: Int, samplingModeValue1: Int, samplingModeValue2: Int, localMatrix: InteropPointer): NativePointer
