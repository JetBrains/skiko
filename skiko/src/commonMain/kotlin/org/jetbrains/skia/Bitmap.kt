package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import kotlin.jvm.JvmStatic
import kotlin.math.min

class Bitmap internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR), IHasImageInfo {
    companion object {
        @JvmStatic
        fun makeFromImage(image: Image): Bitmap {
            val bitmap = Bitmap()
            bitmap.allocPixels(image.imageInfo)
            return if (image.readPixels(bitmap)) bitmap else {
                bitmap.close()
                throw RuntimeException("Failed to readPixels from $image")
            }
        }

        @JvmStatic
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
    fun makeClone(): Bitmap = nativeCall {
        Bitmap(_nMakeClone(_ptr))
    }

    /**
     * Swaps the fields of the two bitmaps.
     *
     * @param other  Bitmap exchanged with original
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_swap](https://fiddle.skia.org/c/@Bitmap_swap)
     */
    fun swap(other: Bitmap) = nativeCall(other) { other ->
        _nSwap(_ptr, other._ptr)
        _imageInfo = null
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
        get() = nativeCall { _nGetRowBytesAsPixels(_ptr) }

    /**
     * Returns true if PixelRef is null.
     *
     * Does not check if width or height are zero; call [.drawsNothing]
     * to check width, height, and PixelRef.
     *
     * @return  true if no PixelRef is associated
     */
    val isNull: Boolean
        get() = nativeCall { _nIsNull(_ptr) }

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
        get() = nativeCall { _nGetRowBytes(_ptr) }

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
    fun setAlphaType(alphaType: ColorAlphaType): Boolean = nativeCall {
        _imageInfo = null
        _nSetAlphaType(_ptr, alphaType.ordinal)
    }

    /**
     * Returns minimum memory required for pixel storage.
     * Does not include unused memory on last row when getRowBytesAsPixels() exceeds getWidth().
     * Returns zero if height() or width() is 0.
     * Returns getHeight() times getRowBytes() if getColorType() is [ColorType.UNKNOWN].
     *
     * @return  size in bytes of image buffer
     */
    fun computeByteSize(): Int = nativeCall { _nComputeByteSize(_ptr) }

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
        get() = nativeCall { _nIsImmutable(_ptr) }

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
    fun computeIsOpaque(): Boolean = nativeCall { _nComputeIsOpaque(_ptr) }

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
    fun setImageInfo(imageInfo: ImageInfo, rowBytes: Int): Boolean =
        nativeCall(imageInfo.colorInfo.colorSpace) { colorSpace ->
            _imageInfo = null
            _nSetImageInfo(
                _ptr,
                imageInfo.width,
                imageInfo.height,
                imageInfo.colorInfo.colorType.ordinal,
                imageInfo.colorInfo.alphaType.ordinal,
                colorSpace.pointerOrNullPointer,
                rowBytes
            )
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
    fun allocPixelsFlags(imageInfo: ImageInfo, zeroPixels: Boolean): Boolean =
        nativeCall(imageInfo.colorInfo.colorSpace) { colorSpace ->
            _imageInfo = null
            _nAllocPixelsFlags(
                _ptr,
                imageInfo.width,
                imageInfo.height,
                imageInfo.colorInfo.colorType.ordinal,
                imageInfo.colorInfo.alphaType.ordinal,
                colorSpace.pointerOrNullPointer,
                if (zeroPixels) 1 else 0
            )
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
    fun allocPixels(info: ImageInfo, rowBytes: Int): Boolean = nativeCall(info.colorInfo.colorSpace) { colorSpace ->
        _imageInfo = null
        _nAllocPixelsRowBytes(
            _ptr,
            info.width,
            info.height,
            info.colorInfo.colorType.ordinal,
            info.colorInfo.alphaType.ordinal,
            colorSpace.pointerOrNullPointer,
            rowBytes
        )
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
    ): Boolean = nativeCall(info.colorInfo.colorSpace) { colorSpace ->
        _imageInfo = null
        interopScope {
            _nInstallPixels(
                _ptr,
                info.width,
                info.height,
                info.colorInfo.colorType.ordinal,
                info.colorInfo.alphaType.ordinal,
                colorSpace.pointerOrNullPointer,
                toInterop(pixels),
                rowBytes,
                pixels?.size ?: 0
            )
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
    fun allocPixels(): Boolean = nativeCall {
        _imageInfo = null
        _nAllocPixels(_ptr)
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
        get() = nativeCall {
            val res = _nGetPixelRef(_ptr)
            if (res == NullPointer) null else PixelRef(res)
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
        get() = nativeCall {
            val resX = _nGetPixelRefOriginX(_ptr)
            val resY = _nGetPixelRefOriginY(_ptr)
            IPoint(resX, resY)
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
    fun setPixelRef(pixelRef: PixelRef?, dx: Int, dy: Int): Bitmap = nativeCall(pixelRef) {pixelRef ->
        _imageInfo = null
        _nSetPixelRef(
            _ptr,
            pixelRef.pointerOrNullPointer,
            dx,
            dy
        )
        this
    }

    /**
     * Returns true if Bitmap can be drawn.
     *
     * @return  true if getPixels() is not null
     */
    val isReadyToDraw: Boolean
        get() = nativeCall { _nIsReadyToDraw(_ptr) }

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
        get() = nativeCall { _nGetGenerationId(_ptr) }

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
    fun getColor(x: Int, y: Int): Int = nativeCall { Bitmap_nGetColor(_ptr, x, y) }

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
    fun getAlphaf(x: Int, y: Int): Float = nativeCall { _nGetAlphaf(_ptr, x, y) }

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
    fun extractSubset(dst: Bitmap, subset: IRect): Boolean = nativeCall(dst) { dst ->
        _nExtractSubset(
            _ptr,
            dst._ptr,
            subset.left,
            subset.top,
            subset.right,
            subset.bottom
        )
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
        return nativeCall(dstInfo.colorInfo.colorSpace) { colorSpace ->
            interopScope {
                val byteArrayHandle = toInteropForResult(byteArray)
                val successfulRead = _nReadPixels(
                    _ptr,
                    dstInfo.width,
                    dstInfo.height,
                    dstInfo.colorInfo.colorType.ordinal,
                    dstInfo.colorInfo.alphaType.ordinal,
                    colorSpace.pointerOrNullPointer,
                    dstRowBytes,
                    srcX,
                    srcY,
                    byteArrayHandle
                )
                if (successfulRead) {
                    byteArrayHandle.fromInterop(byteArray)
                }
                successfulRead
            }
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
    fun extractAlpha(dst: Bitmap, paint: Paint?): IPoint? = nativeCall(dst, paint) { dst, paint ->
        withNullableResult(IntArray(2)) {
            _nExtractAlpha(
                ptr = _ptr,
                dstPtr = dst._ptr,
                paintPtr = paint.pointerOrNullPointer,
                iPointResultIntArray = it
            )
        }?.let {
            IPoint(x = it[0], y = it[1])
        }
    }

    /**
     * Create a pixmap and copy buffer contents into it
     *
     * @return Pixamp with copied pixels, or null
     *
     * @see [https://fiddle.skia.org/c/@Bitmap_peekPixels](https://fiddle.skia.org/c/@Bitmap_peekPixels)
     */
    fun peekPixels(): Pixmap? = nativeCall {
        val res = _nPeekPixels(_ptr)
        if (res == NullPointer) null else Pixmap(res, true)
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
    ): Shader = nativeCall {
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
    }

    private object _FinalizerHolder {
        val PTR = Bitmap_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetFinalizer")
// No ForceNativeThreadState: returns the function that frees the object, without calling it, so it is short and cannot
// block.
// Native call path: deleteBitmap
private external fun Bitmap_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nMake")
// No ForceNativeThreadState: creates an empty bitmap with no pixel storage and empty pixel layout information, so it is
// short and cannot block.
// Native call path: SkBitmap::SkBitmap
private external fun _nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nMakeClone")
// No ForceNativeThreadState: copies the bitmap and bumps the pixel reference count without copying pixels, so it is
// short and cannot block.
// Native call path: SkBitmap::SkBitmap
private external fun _nMakeClone(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSwap")
// No ForceNativeThreadState: swaps the fields of two bitmaps without changing any reference counts, so it is short and
// cannot block.
// Native call path: SkBitmap::swap
private external fun _nSwap(ptr: NativePointer, otherPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetImageInfo")
// No ForceNativeThreadState: copies the image info and adds a reference to its color space, so it is short and cannot
// block.
// Native call path: SkImageInfo::refColorSpace
private external fun _nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetRowBytesAsPixels")
// No ForceNativeThreadState: returns the stored row size converted to pixels, so it is short and cannot block.
// Native call path: SkBitmap::rowBytesAsPixels -> SkPixmap::rowBytesAsPixels
private external fun _nGetRowBytesAsPixels(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsNull")
// No ForceNativeThreadState: checks whether the pixel reference is null, so it is short and cannot block.
// Native call path: SkBitmap::isNull
private external fun _nIsNull(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetRowBytes")
// No ForceNativeThreadState: returns the stored row size in bytes, so it is short and cannot block.
// Native call path: SkBitmap::rowBytes
private external fun _nGetRowBytes(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetAlphaType")
// No ForceNativeThreadState: checks the alpha type and rewrites the stored image info without touching any pixels, so
// it is short and cannot block.
// Native call path: SkBitmap::setAlphaType -> SkPixmap::reset
private external fun _nSetAlphaType(ptr: NativePointer, alphaType: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nComputeByteSize")
// No ForceNativeThreadState: does integer math on height and row bytes with an overflow check, so it is short and
// cannot block.
// Native call path: SkBitmap::computeByteSize -> SkImageInfo::computeByteSize
private external fun _nComputeByteSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsImmutable")
// No ForceNativeThreadState: reads the stored immutability flag of the pixel reference, so it is short and cannot
// block.
// Native call path: SkBitmap::isImmutable
private external fun _nIsImmutable(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetImmutable")
// No ForceNativeThreadState: stores an immutable flag on the pixel reference, so it is short and cannot block.
// Native call path: SkBitmap::setImmutable -> SkPixelRef::setImmutable
private external fun _nSetImmutable(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nReset")
// Needs ForceNativeThreadState: releasing old pixel storage notifies change listeners, including GPU caches, so time
// grows with the listener count.
// Native call path: SkBitmap::reset -> ~SkPixelRef -> callGenIDChangeListeners -> SkIDChangeListener::List::changed
@ForceNativeThreadState
private external fun _nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nComputeIsOpaque")
// Needs ForceNativeThreadState: scans every pixel, so its time grows with the bitmap size.
// Native call path: SkPixmap::computeIsOpaque
@ForceNativeThreadState
private external fun _nComputeIsOpaque(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetImageInfo")
// Needs ForceNativeThreadState: releasing old pixel storage notifies change listeners, including GPU caches, so time
// grows with the listener count.
// Native call path: SkBitmap::setInfo -> ~SkPixelRef -> callGenIDChangeListeners -> SkIDChangeListener::List::changed
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: releasing old pixel storage notifies change listeners, including GPU caches, so time
// grows with the listener count.
// Native call path: tryAllocPixelsFlags -> ~SkPixelRef -> callGenIDChangeListeners -> SkIDChangeListener::List::changed
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: releasing old pixel storage notifies change listeners, including GPU caches, so time
// grows with the listener count.
// Native call path: tryAllocPixels -> ~SkPixelRef -> callGenIDChangeListeners -> SkIDChangeListener::List::changed
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: copies the whole pixel array into newly allocated memory, so its time grows with the
// image size.
// Native call path: malloc -> memcpy -> SkBitmap::installPixels
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: releasing old pixel storage notifies change listeners, including GPU caches, so time
// grows with the listener count.
// Native call path: tryAllocPixels -> setPixelRef -> ~SkPixelRef -> SkIDChangeListener::List::changed
@ForceNativeThreadState
private external fun _nAllocPixels(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixelRef")
// No ForceNativeThreadState: increments the pixel reference count, so it is short and cannot block.
// Native call path: SkBitmap::pixelRef -> SkPixelRef::ref
private external fun _nGetPixelRef(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixelRefOriginX")
// No ForceNativeThreadState: computes the origin from pointer and row size arithmetic, so it is short and cannot block.
// Native call path: SkBitmap::pixelRefOrigin
private external fun _nGetPixelRefOriginX(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetPixelRefOriginY")
// No ForceNativeThreadState: computes the origin from pointer and row size arithmetic, so it is short and cannot block.
// Native call path: SkBitmap::pixelRefOrigin
private external fun _nGetPixelRefOriginY(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nSetPixelRef")
// Needs ForceNativeThreadState: releasing old pixel storage notifies change listeners, including GPU caches, so time
// grows with the listener count.
// Native call path: SkBitmap::setPixelRef -> ~SkPixelRef -> callGenIDChangeListeners ->
//   SkIDChangeListener::List::changed
@ForceNativeThreadState
private external fun _nSetPixelRef(ptr: NativePointer, pixelRefPtr: NativePointer, dx: Int, dy: Int)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nIsReadyToDraw")
// No ForceNativeThreadState: checks whether pixels are present, so it is short and cannot block.
// Native call path: SkBitmap::readyToDraw
private external fun _nIsReadyToDraw(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetGenerationId")
// No ForceNativeThreadState: reads the generation id, or atomically assigns one if it is not set yet, so it is short
// and cannot block.
// Native call path: SkBitmap::getGenerationID -> SkPixelRef::getGenerationID
private external fun _nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nNotifyPixelsChanged")
// Needs ForceNativeThreadState: notifies every change listener, and the number of listeners is not fixed because GPU
// caches add their own.
// Native call path: SkPixelRef::notifyPixelsChanged -> SkIDChangeListener::List::changed
@ForceNativeThreadState
private external fun _nNotifyPixelsChanged(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nEraseColor")
// Needs ForceNativeThreadState: fills every pixel of the bitmap, so its time grows with the bitmap size.
// Native call path: SkBitmap::erase -> SkPixmap::erase
@ForceNativeThreadState
private external fun _nEraseColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nErase")
// Needs ForceNativeThreadState: fills the pixels of the given rectangle one by one, so its time grows with the
// rectangle area.
// Native call path: SkBitmap::erase -> SkPixmap::erase
@ForceNativeThreadState
private external fun _nErase(ptr: NativePointer, color: Int, left: Int, top: Int, right: Int, bottom: Int)

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetColor")
// No ForceNativeThreadState: reads the color of one pixel through a color type switch, so it is short and cannot block.
// Native call path: SkPixmap::getColor -> SkPixmap::getColor4f
private external fun Bitmap_nGetColor(ptr: NativePointer, x: Int, y: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nGetAlphaf")
// No ForceNativeThreadState: reads the alpha of one pixel through a color type switch, so it is short and cannot block.
// Native call path: SkPixmap::getAlphaf
private external fun _nGetAlphaf(ptr: NativePointer, x: Int, y: Int): Float

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nExtractSubset")
// Needs ForceNativeThreadState: releasing old pixel storage notifies change listeners, including GPU caches, so time
// grows with the listener count.
// Native call path: SkBitmap::extractSubset -> ~SkPixelRef -> callGenIDChangeListeners ->
//   SkIDChangeListener::List::changed
@ForceNativeThreadState
private external fun _nExtractSubset(ptr: NativePointer, dstPtr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nReadPixels")
// Needs ForceNativeThreadState: converts the whole pixel rectangle into the destination, so its time grows with the
// image size.
// Native call path: SkPixmap::readPixels -> SkConvertPixels
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: converts every pixel to alpha and can run an optional mask filter, so its time grows
// with the image size.
// Native call path: SkBitmap::extractAlpha -> SkConvertPixels -> SkMaskFilter::filterMask
@ForceNativeThreadState
private external fun _nExtractAlpha(ptr: NativePointer, dstPtr: NativePointer, paintPtr: NativePointer, iPointResultIntArray: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nPeekPixels")
// No ForceNativeThreadState: copies pixel layout information into a new object without accessing pixels, so it is short
// and cannot block.
// Native call path: SkBitmap::peekPixels
private external fun _nPeekPixels(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Bitmap__1nMakeShader")
// Needs ForceNativeThreadState: copies all the pixels of a mutable bitmap into a new image, so its time grows with the
// bitmap size.
// Native call path: SkImage_Raster::MakeFromBitmap -> SkData::MakeWithCopy
@ForceNativeThreadState
private external fun _nMakeShader(ptr: NativePointer, tmx: Int, tmy: Int, samplingModeValue1: Int, samplingModeValue2: Int, localMatrix: InteropPointer): NativePointer
