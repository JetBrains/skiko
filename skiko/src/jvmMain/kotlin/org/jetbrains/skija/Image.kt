package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.RuntimeException
import java.lang.ref.Reference
import java.nio.ByteBuffer

class Image internal constructor(ptr: Long) : RefCnt(ptr), IHasImageInfo {
    companion object {
        /**
         *
         * Creates Image from pixels.
         *
         *
         * Image is returned if pixels are valid. Valid Pixmap parameters include:
         *
         *  * dimensions are greater than zero;
         *  * each dimension fits in 29 bits;
         *  * ColorType and AlphaType are valid, and ColorType is not ColorType.UNKNOWN;
         *  * row bytes are large enough to hold one row of pixels;
         *  * pixel address is not null.
         *
         *
         * @param imageInfo  ImageInfo
         * @param bytes      pixels array
         * @param rowBytes   how many bytes in a row
         * @return           Image
         *
         * @see [https://fiddle.skia.org/c/@Image_MakeRasterCopy](https://fiddle.skia.org/c/@Image_MakeRasterCopy)
         */
        fun makeRaster(imageInfo: ImageInfo, bytes: ByteArray, rowBytes: Long): Image {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeRaster(
                    imageInfo.width,
                    imageInfo.height,
                    imageInfo.colorInfo.colorType.ordinal,
                    imageInfo.colorInfo.alphaType.ordinal,
                    Native.Companion.getPtr(imageInfo.colorInfo.colorSpace),
                    bytes,
                    rowBytes
                )
                if (ptr == 0L) throw RuntimeException("Failed to makeRaster $imageInfo $bytes $rowBytes")
                Image(ptr)
            } finally {
                Reference.reachabilityFence(imageInfo.colorInfo.colorSpace)
            }
        }

        /**
         *
         * Creates Image from pixels.
         *
         *
         * Image is returned if pixels are valid. Valid Pixmap parameters include:
         *
         *  * dimensions are greater than zero;
         *  * each dimension fits in 29 bits;
         *  * ColorType and AlphaType are valid, and ColorType is not ColorType.UNKNOWN;
         *  * row bytes are large enough to hold one row of pixels;
         *  * pixel address is not null.
         *
         *
         * @param imageInfo  ImageInfo
         * @param data       pixels array
         * @param rowBytes   how many bytes in a row
         * @return           Image
         */
        fun makeRaster(imageInfo: ImageInfo, data: Data, rowBytes: Long): Image {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeRasterData(
                    imageInfo.width,
                    imageInfo.height,
                    imageInfo.colorInfo.colorType.ordinal,
                    imageInfo.colorInfo.alphaType.ordinal,
                    Native.getPtr(imageInfo.colorInfo.colorSpace),
                    Native.getPtr(data),
                    rowBytes
                )
                if (ptr == 0L) throw RuntimeException("Failed to makeRaster $imageInfo $data $rowBytes")
                Image(ptr)
            } finally {
                Reference.reachabilityFence(imageInfo.colorInfo.colorSpace)
                Reference.reachabilityFence(data)
            }
        }

        /**
         *
         * Creates Image from bitmap, sharing or copying bitmap pixels. If the bitmap
         * is marked immutable, and its pixel memory is shareable, it may be shared
         * instead of copied.
         *
         *
         * Image is returned if bitmap is valid. Valid Bitmap parameters include:
         *
         *  * dimensions are greater than zero;
         *  * each dimension fits in 29 bits;
         *  * ColorType and AlphaType are valid, and ColorType is not ColorType.UNKNOWN;
         *  * row bytes are large enough to hold one row of pixels;
         *  * pixel address is not nullptr.
         *
         *
         * @param bitmap  ImageInfo, row bytes, and pixels
         * @return        created Image
         *
         * @see [https://fiddle.skia.org/c/@Image_MakeFromBitmap](https://fiddle.skia.org/c/@Image_MakeFromBitmap)
         */
        fun makeFromBitmap(bitmap: Bitmap): Image {
            return try {
                assert(bitmap != null) { "Can’t makeFromBitmap with bitmap == null" }
                Stats.onNativeCall()
                val ptr =
                    _nMakeFromBitmap(Native.Companion.getPtr(bitmap))
                if (ptr == 0L) throw RuntimeException("Failed to Image::makeFromBitmap $bitmap")
                Image(ptr)
            } finally {
                Reference.reachabilityFence(bitmap)
            }
        }

        fun makeFromPixmap(pixmap: Pixmap): Image {
            return try {
                assert(pixmap != null) { "Can’t makeFromPixmap with pixmap == null" }
                Stats.onNativeCall()
                val ptr =
                    _nMakeFromPixmap(Native.Companion.getPtr(pixmap))
                if (ptr == 0L) throw RuntimeException("Failed to Image::makeFromRaster $pixmap")
                Image(ptr)
            } finally {
                Reference.reachabilityFence(pixmap)
            }
        }

        fun makeFromEncoded(bytes: ByteArray?): Image {
            Stats.onNativeCall()
            val ptr = _nMakeFromEncoded(bytes)
            require(ptr != 0L) { "Failed to Image::makeFromEncoded" }
            return Image(ptr)
        }

        @JvmStatic external fun _nMakeRaster(
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            pixels: ByteArray?,
            rowBytes: Long
        ): Long

        @JvmStatic external fun _nMakeRasterData(
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            dataPtr: Long,
            rowBytes: Long
        ): Long

        @JvmStatic external fun _nMakeFromBitmap(bitmapPtr: Long): Long
        @JvmStatic external fun _nMakeFromPixmap(pixmapPtr: Long): Long
        @JvmStatic external fun _nMakeFromEncoded(bytes: ByteArray?): Long
        @JvmStatic external fun _nGetImageInfo(ptr: Long): ImageInfo?
        @JvmStatic external fun _nEncodeToData(ptr: Long, format: Int, quality: Int): Long
        @JvmStatic external fun _nMakeShader(ptr: Long, tmx: Int, tmy: Int, samplingMode: Long, localMatrix: FloatArray?): Long
        @JvmStatic external fun _nPeekPixels(ptr: Long): ByteBuffer?
        @JvmStatic external fun _nPeekPixelsToPixmap(ptr: Long, pixmapPtr: Long): Boolean
        @JvmStatic external fun _nScalePixels(ptr: Long, pixmapPtr: Long, samplingOptions: Long, cache: Boolean): Boolean
        @JvmStatic external fun _nReadPixelsBitmap(
            ptr: Long,
            contextPtr: Long,
            bitmapPtr: Long,
            srcX: Int,
            srcY: Int,
            cache: Boolean
        ): Boolean

        @JvmStatic external fun _nReadPixelsPixmap(ptr: Long, pixmapPtr: Long, srcX: Int, srcY: Int, cache: Boolean): Boolean

        init {
            staticLoad()
        }
    }

    internal var _imageInfo: ImageInfo? = null

    /**
     * Returns a ImageInfo describing the width, height, color type, alpha type, and color space
     * of the Image.
     *
     * @return  image info of Image.
     */
    override val imageInfo: ImageInfo
        get() = try {
            if (_imageInfo == null) {
                synchronized(this) {
                    if (_imageInfo == null) {
                        Stats.onNativeCall()
                        _imageInfo = _nGetImageInfo(_ptr)
                    }
                }
            }
            _imageInfo!!
        } finally {
            Reference.reachabilityFence(this)
        }
    /**
     * Encodes Image pixels, returning result as Data.
     *
     * Returns null if encoding fails, or if format is not supported.
     *
     * On a macOS, encodedImageFormat can additionally be one of:
     * [EncodedImageFormat.ICO], [EncodedImageFormat.BMP] or [EncodedImageFormat.GIF].
     *
     * quality is a platform and format specific metric trading off size and encoding
     * error. When used, quality equaling 100 encodes with the least error. quality may
     * be ignored by the encoder.
     *
     * @param format   one of: [EncodedImageFormat.JPEG], [EncodedImageFormat.PNG], [EncodedImageFormat.WEBP]
     * @param quality  encoder specific metric with 100 equaling best
     * @return         encoded Image, or null
     *
     * @see [https://fiddle.skia.org/c/@Image_encodeToData](https://fiddle.skia.org/c/@Image_encodeToData)
     */
    /**
     *
     * Encodes Image pixels, returning result as Data. Returns existing encoded data
     * if present; otherwise, Image is encoded with [EncodedImageFormat.PNG].
     *
     *
     * Returns null if existing encoded data is missing or invalid, and encoding fails.
     *
     * @return  encoded Image, or null
     *
     * @see [https://fiddle.skia.org/c/@Image_encodeToData_2](https://fiddle.skia.org/c/@Image_encodeToData_2)
     */
    @JvmOverloads
    fun encodeToData(format: EncodedImageFormat = EncodedImageFormat.PNG, quality: Int = 100): Data? {
        return try {
            Stats.onNativeCall()
            val ptr = _nEncodeToData(_ptr, format.ordinal, quality)
            if (ptr == 0L) null else org.jetbrains.skija.Data(ptr)
        } finally {
            Reference.reachabilityFence(this)
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

    @JvmOverloads
    fun makeShader(
        tmx: FilterTileMode = FilterTileMode.CLAMP,
        tmy: FilterTileMode = FilterTileMode.CLAMP,
        sampling: SamplingMode = SamplingMode.Companion.DEFAULT,
        localMatrix: Matrix33? = null
    ): Shader {
        return try {
            assert(tmx != null) { "Can’t Bitmap.makeShader with tmx == null" }
            assert(tmy != null) { "Can’t Bitmap.makeShader with tmy == null" }
            assert(sampling != null) { "Can’t Bitmap.makeShader with sampling == null" }
            Stats.onNativeCall()
            Shader(
                _nMakeShader(
                    _ptr,
                    tmx.ordinal,
                    tmy.ordinal,
                    sampling._pack(),
                    localMatrix?.mat
                )
            )
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * If pixel address is available, return ByteBuffer wrapping it.
     * If pixel address is not available, return null.
     *
     * @return  ByteBuffer with direct access to pixels, or null
     *
     * @see [https://fiddle.skia.org/c/@Image_peekPixels](https://fiddle.skia.org/c/@Image_peekPixels)
     */
    fun peekPixels(): ByteBuffer? {
        return try {
            Stats.onNativeCall()
            _nPeekPixels(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun peekPixels(pixmap: Pixmap?): Boolean {
        return try {
            Stats.onNativeCall()
            _nPeekPixelsToPixmap(
                _ptr,
                Native.Companion.getPtr(pixmap)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(pixmap)
        }
    }

    fun readPixels(dst: Bitmap): Boolean {
        return readPixels(null, dst, 0, 0, false)
    }

    fun readPixels(dst: Bitmap, srcX: Int, srcY: Int): Boolean {
        return readPixels(null, dst, srcX, srcY, false)
    }

    /**
     *
     * Copies Rect of pixels from Image to Bitmap. Copy starts at offset (srcX, srcY),
     * and does not exceed Image (getWidth(), getHeight()).
     *
     *
     * dst specifies width, height, ColorType, AlphaType, and ColorSpace of destination.
     *
     *
     * Returns true if pixels are copied. Returns false if:
     *
     *  * dst has no pixels allocated.
     *
     *
     *
     * Pixels are copied only if pixel conversion is possible. If Image ColorType is
     * ColorType.GRAY_8, or ColorType.ALPHA_8; dst.getColorType() must match.
     * If Image ColorType is ColorType.GRAY_8, dst.getColorSpace() must match.
     * If Image AlphaType is AlphaType.OPAQUE, dst.getAlphaType() must
     * match. If Image ColorSpace is null, dst.getColorSpace() must match. Returns
     * false if pixel conversion is not possible.
     *
     *
     * srcX and srcY may be negative to copy only top or left of source. Returns
     * false if getWidth() or getHeight() is zero or negative.
     *
     *
     * Returns false if abs(srcX) &gt;= Image.getWidth(), or if abs(srcY) &gt;= Image.getHeight().
     *
     *
     * If cache is true, pixels may be retained locally, otherwise pixels are not added to the local cache.
     *
     * @param context the DirectContext in play, if it exists
     * @param dst     destination bitmap
     * @param srcX    column index whose absolute value is less than getWidth()
     * @param srcY    row index whose absolute value is less than getHeight()
     * @param cache   whether the pixels should be cached locally
     * @return        true if pixels are copied to dstPixels
     */
    fun readPixels(context: DirectContext?, dst: Bitmap, srcX: Int, srcY: Int, cache: Boolean): Boolean {
        return try {
            assert(dst != null) { "Can’t readPixels with dst == null" }
            _nReadPixelsBitmap(
                _ptr,
                Native.Companion.getPtr(context),
                Native.Companion.getPtr(dst),
                srcX,
                srcY,
                cache
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(context)
            Reference.reachabilityFence(dst)
        }
    }

    fun readPixels(dst: Pixmap, srcX: Int, srcY: Int, cache: Boolean): Boolean {
        return try {
            assert(dst != null) { "Can’t readPixels with dst == null" }
            _nReadPixelsPixmap(
                _ptr,
                Native.Companion.getPtr(dst),
                srcX,
                srcY,
                cache
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(dst)
        }
    }

    fun scalePixels(dst: Pixmap, samplingMode: SamplingMode, cache: Boolean): Boolean {
        return try {
            assert(dst != null) { "Can’t scalePixels with dst == null" }
            _nScalePixels(
                _ptr,
                Native.Companion.getPtr(dst),
                samplingMode._pack(),
                cache
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(dst)
        }
    }
}