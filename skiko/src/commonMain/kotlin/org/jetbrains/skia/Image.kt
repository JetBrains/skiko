package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skiko.InternalSkikoApi

class Image @InternalSkikoApi constructor(ptr: NativePointer) : RefCnt(ptr), IHasImageInfo {
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
        fun makeRaster(imageInfo: ImageInfo, bytes: ByteArray, rowBytes: Int): Image {
            return try {
                Stats.onNativeCall()
                val ptr = interopScope {
                    _nMakeRaster(
                        imageInfo.width,
                        imageInfo.height,
                        imageInfo.colorInfo.colorType.ordinal,
                        imageInfo.colorInfo.alphaType.ordinal,
                        getPtr(imageInfo.colorInfo.colorSpace),
                        toInterop(bytes),
                        rowBytes
                    )
                }
                if (ptr == NullPointer) throw RuntimeException("Failed to makeRaster $imageInfo $bytes $rowBytes")
                Image(ptr)
            } finally {
                reachabilityBarrier(imageInfo.colorInfo.colorSpace)
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
        fun makeRaster(imageInfo: ImageInfo, data: Data, rowBytes: Int): Image {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeRasterData(
                    imageInfo.width,
                    imageInfo.height,
                    imageInfo.colorInfo.colorType.ordinal,
                    imageInfo.colorInfo.alphaType.ordinal,
                    getPtr(imageInfo.colorInfo.colorSpace),
                    getPtr(data),
                    rowBytes
                )
                if (ptr == NullPointer) throw RuntimeException("Failed to makeRaster $imageInfo $data $rowBytes")
                Image(ptr)
            } finally {
                reachabilityBarrier(imageInfo.colorInfo.colorSpace)
                reachabilityBarrier(data)
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
                Stats.onNativeCall()
                val ptr = _nMakeFromBitmap(getPtr(bitmap))
                if (ptr == NullPointer) throw RuntimeException("Failed to Image::makeFromBitmap $bitmap")
                Image(ptr)
            } finally {
                reachabilityBarrier(bitmap)
            }
        }

        fun makeFromPixmap(pixmap: Pixmap): Image {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeFromPixmap(getPtr(pixmap))
                if (ptr == NullPointer) throw RuntimeException("Failed to Image::makeFromRaster $pixmap")
                Image(ptr)
            } finally {
                reachabilityBarrier(pixmap)
            }
        }

        fun makeFromEncoded(bytes: ByteArray): Image {
            Stats.onNativeCall()
            val ptr = interopScope {
                _nMakeFromEncoded(toInterop(bytes), bytes.size)
            }
            require(ptr != NullPointer) { "Failed to Image::makeFromEncoded" }
            return Image(ptr)
        }

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
                commonSynchronized(this) {
                    if (_imageInfo == null) {
                        _imageInfo = ImageInfo.createUsing(
                            _ptr = _ptr,
                            _nGetImageInfo = ::Image_nGetImageInfo
                        )
                    }
                }
            }
            _imageInfo!!
        } finally {
            reachabilityBarrier(this)
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
     * be ignored by the encoder. PNG is lossless, so quality is ignored for PNG. Use
     * `pngCompressionLevel` to control PNG compression.
     *
     * pngCompressionLevel is used only for PNG and is passed as zlib compression level.
     * It must be in the 0..9 range, where 6 is the default, 9 is maximal compression,
     * and 0 skips zlib compression.
     *
     * @param format               one of: [EncodedImageFormat.JPEG], [EncodedImageFormat.PNG], [EncodedImageFormat.WEBP]
     * @param quality              encoder specific metric with 100 equaling best, ignored for PNG
     * @param pngCompressionLevel  PNG zlib compression level in 0..9
     * @return                     encoded Image, or null
     *
     */
    fun encodeToData(
        format: EncodedImageFormat = EncodedImageFormat.PNG,
        quality: Int = 100,
        pngCompressionLevel: Int = 6
    ): Data? {
        return try {
            Stats.onNativeCall()
            val ptr = _nEncodeToData(_ptr, format.ordinal, quality, pngCompressionLevel)
            if (ptr == NullPointer) null else org.jetbrains.skia.Data(ptr)
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
                    Image_nMakeShader(
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

    /**
     * If pixel address is available, return [Pixmap].
     * If pixel address is not available, return null.
     *
     * @see [https://fiddle.skia.org/c/@Image_peekPixels](https://fiddle.skia.org/c/@Image_peekPixels)
     */
    fun peekPixels(): Pixmap? {
        return try {
            Stats.onNativeCall()
            Image_nPeekPixels(_ptr).takeIf {
                it != NullPointer
            }?.let {
                Pixmap(it, true)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun peekPixels(pixmap: Pixmap?): Boolean {
        return try {
            Stats.onNativeCall()
            _nPeekPixelsToPixmap(
                _ptr,
                getPtr(pixmap)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(pixmap)
        }
    }

    fun readPixels(dst: Bitmap): Boolean = readPixels(dst, 0, 0, false)

    fun readPixels(dst: Bitmap, srcX: Int, srcY: Int): Boolean = readPixels(dst, srcX, srcY, false)


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
     * @param dst     destination bitmap
     * @param srcX    column index whose absolute value is less than getWidth()
     * @param srcY    row index whose absolute value is less than getHeight()
     * @param cache   whether the pixels should be cached locally
     * @return        true if pixels are copied to dstPixels
     */
    fun readPixels(dst: Bitmap, srcX: Int, srcY: Int, cache: Boolean): Boolean {
        return try {
            _nReadPixelsBitmap(
                _ptr,
                NullPointer,
                getPtr(dst),
                srcX,
                srcY,
                cache
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dst)
        }
    }

    fun readPixels(dst: Pixmap, srcX: Int, srcY: Int, cache: Boolean): Boolean {
        return try {
            _nReadPixelsPixmap(
                _ptr,
                getPtr(dst),
                srcX,
                srcY,
                cache
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dst)
        }
    }

    fun scalePixels(dst: Pixmap, samplingMode: SamplingMode, cache: Boolean): Boolean {
        return try {
            _nScalePixels(
                _ptr,
                getPtr(dst),
                samplingMode._packedInt1(),
                samplingMode._packedInt2(),
                cache
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(dst)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_Image__1nGetImageInfo")
// No ForceNativeThreadState: copies the stored image details and adds a reference to its color space, so it is short
// and cannot block.
// Native call path: SkImage::imageInfo -> writeImageInfoForInterop
private external fun Image_nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeShader")
// No ForceNativeThreadState: wraps the image in a shader object that stores references, tile modes and a matrix, so it
// is short and cannot block.
// Native call path: SkImage::makeShader -> SkImageShader::Make
private external fun Image_nMakeShader(ptr: NativePointer, tmx: Int, tmy: Int, samplingModeVal1: Int, samplingModeVal2: Int, localMatrix: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nPeekPixels")
// No ForceNativeThreadState: exposes the stored pixels of a raster image and reports failure for other kinds of image,
// so it is short and cannot block.
// Native call path: SkImage::peekPixels -> SkImage_Raster::onPeekPixels
private external fun Image_nPeekPixels(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeRaster")
// Needs ForceNativeThreadState: copies the whole pixel buffer of the image, so its time grows with the image size.
// Native call path: SkImages::RasterFromPixmapCopy -> SkData::MakeWithCopy
@ForceNativeThreadState
private external fun _nMakeRaster(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    pixels: InteropPointer,
    rowBytes: Int
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeRasterData")
// No ForceNativeThreadState: wraps the caller's pixel data without copying it, so it is short and cannot block.
// Native call path: SkImages::RasterFromData -> SkImage_Raster::SkImage_Raster
private external fun _nMakeRasterData(
    width: Int,
    height: Int,
    colorType: Int,
    alphaType: Int,
    colorSpacePtr: NativePointer,
    dataPtr: NativePointer,
    rowBytes: Int
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeFromBitmap")
// Needs ForceNativeThreadState: copies the pixels of a mutable bitmap, so its time grows with the bitmap size.
// Native call path: SkImages::RasterFromBitmap -> SkImage_Raster::MakeFromBitmap
@ForceNativeThreadState
private external fun _nMakeFromBitmap(bitmapPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeFromPixmap")
// No ForceNativeThreadState: wraps the pixels without copying them, so it is short and cannot block.
// Native call path: SkImages::RasterFromPixmap -> SkData::MakeWithProc
private external fun _nMakeFromPixmap(pixmapPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nMakeFromEncoded")
// Needs ForceNativeThreadState: copies the whole encoded file and parses its header, so its time grows with the file
// size.
// Native call path: SkData::MakeWithCopy -> SkImages::DeferredFromEncodedData -> SkCodec::MakeFromData
@ForceNativeThreadState
internal external fun _nMakeFromEncoded(bytes: InteropPointer, encodedLength: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nEncodeToData")
// Needs ForceNativeThreadState: compresses the whole image as PNG, JPEG or WebP, so its time grows with the image size,
// and a lazily loaded image is decoded first.
// Native call path: SkPngEncoder::Encode
@ForceNativeThreadState
private external fun _nEncodeToData(
    ptr: NativePointer,
    format: Int,
    quality: Int,
    pngCompressionLevel: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Image__1nPeekPixelsToPixmap")
// No ForceNativeThreadState: exposes the pixels of a raster image without decoding or copying them and reports failure
// for other kinds of image, so it is short and cannot block.
// Native call path: SkImage::peekPixels -> SkImage_Raster::onPeekPixels
private external fun _nPeekPixelsToPixmap(ptr: NativePointer, pixmapPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Image__1nScalePixels")
// Needs ForceNativeThreadState: may decode the image or read it back from the GPU through Metal, and waiting for the
// GPU can take a while.
// Native call path: SkImage::scalePixels -> SkImage_GaneshBase::onReadPixels -> GrMtlGpu::onReadPixels
@ForceNativeThreadState
private external fun _nScalePixels(ptr: NativePointer, pixmapPtr: NativePointer, samplingOptionsVal1: Int, samplingOptionsVal2: Int, cache: Boolean): Boolean

@ExternalSymbolName("org_jetbrains_skia_Image__1nReadPixelsBitmap")
// Needs ForceNativeThreadState: may decode the image or read it back from the GPU through Metal, which makes it wait
// for the GPU; raster pixels are converted in full.
// Native call path: SkImage::readPixels -> SkImage_GaneshBase::onReadPixels -> GrMtlGpu::onReadPixels
@ForceNativeThreadState
private external fun _nReadPixelsBitmap(
    ptr: NativePointer,
    contextPtr: NativePointer,
    bitmapPtr: NativePointer,
    srcX: Int,
    srcY: Int,
    cache: Boolean
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Image__1nReadPixelsPixmap")
// Needs ForceNativeThreadState: may decode the image or read it back from the GPU through Metal, which makes it wait
// for the GPU; raster pixels are converted in full.
// Native call path: SkImage::readPixels -> SkImage_GaneshBase::onReadPixels -> GrMtlGpu::onReadPixels
@ForceNativeThreadState
private external fun _nReadPixelsPixmap(ptr: NativePointer, pixmapPtr: NativePointer, srcX: Int, srcY: Int, cache: Boolean): Boolean

