package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class Codec internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR), IHasImageInfo {
    companion object {
        /**
         * If this data represents an encoded image that we know how to decode,
         * return an Codec that can decode it. Otherwise throws IllegalArgumentException.
         */
        fun makeFromData(data: Data?): Codec {
            return try {
                Stats.onNativeCall()
                val ptr =
                    _nMakeFromData(getPtr(data))
                require(ptr != NullPointer) { "Unsupported format" }
                Codec(ptr)
            } finally {
                reachabilityBarrier(data)
            }
        }

        internal fun _validateResult(result: Int) {
            when (result) {
                1 -> throw IllegalArgumentException("Incomplete input: A partial image was generated.")
                2 -> throw IllegalArgumentException("Error in input")
                3 -> throw IllegalArgumentException("Invalid conversion: The generator cannot convert to match the request, ignoring dimensions")
                4 -> throw IllegalArgumentException("Invalid scale: The generator cannot scale to requested size")
                5 -> throw IllegalArgumentException("Invalid parameter: Parameters (besides info) are invalid. e.g. NULL pixels, rowBytes too small, etc")
                6 -> throw IllegalArgumentException("Invalid input: The input did not contain a valid image")
                7 -> throw UnsupportedOperationException("Could not rewind: Fulfilling this request requires rewinding the input, which is not supported for this input")
                8 -> throw RuntimeException("Internal error")
                9 -> throw UnsupportedOperationException("Unimplemented: This method is not implemented by this codec")
            }
        }

        init {
            staticLoad()
        }
    }

    internal var _imageInfo: ImageInfo? = null
    override val imageInfo: ImageInfo
        get() = try {
            if (_imageInfo == null) {
                _imageInfo = ImageInfo.createUsing(
                    _ptr = _ptr,
                    _nGetImageInfo = ::Codec_nGetImageInfo
                )
            }
            _imageInfo!!
        } finally {
            reachabilityBarrier(this)
        }

    val size: IPoint
        get() = try {
            Stats.onNativeCall()
            IPoint(_nGetSizeWidth(_ptr), _nGetSizeHeight(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    val encodedOrigin: EncodedOrigin
        get() = try {
            Stats.onNativeCall()
            EncodedOrigin.values().get(_nGetEncodedOrigin(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    val encodedImageFormat: EncodedImageFormat
        get() = try {
            Stats.onNativeCall()
            EncodedImageFormat.values().get(_nGetEncodedImageFormat(_ptr))
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Decodes an image into a bitmap.
     *
     * @return  decoded bitmap
     */
    fun readPixels(): Bitmap {
        val bitmap = Bitmap()
        bitmap.allocPixels(imageInfo)
        readPixels(bitmap)
        return bitmap
    }

    /**
     *
     * Decodes an image into a bitmap.
     *
     *
     * Repeated calls to this function should give the same results,
     * allowing the PixelRef to be immutable.
     *
     *
     * Bitmap specifies the description of the format (config, size)
     * expected by the caller.  This can simply be identical
     * to the info returned by getImageInfo().
     *
     *
     * This contract also allows the caller to specify
     * different output-configs, which the implementation can
     * decide to support or not.
     *
     *
     * A size that does not match getImageInfo() implies a request
     * to scale. If the generator cannot perform this scale,
     * it will throw an exception.
     *
     *
     * If the info contains a non-null ColorSpace, the codec
     * will perform the appropriate color space transformation.
     *
     *
     * If the caller passes in the ColorSpace that maps to the
     * ICC profile reported by getICCProfile(), the color space
     * transformation is a no-op.
     *
     *
     * If the caller passes a null SkColorSpace, no color space
     * transformation will be done.
     *
     * @param bitmap      the description of the format (config, size) expected by the caller
     * @return            this
     */
    fun readPixels(bitmap: Bitmap?): Codec {
        return try {
            Stats.onNativeCall()
            _validateResult(
                Codec_nReadPixels(
                    _ptr,
                    getPtr(bitmap),
                    0,
                    -1
                )
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(bitmap)
        }
    }

    /**
     *
     * Decodes a frame in a multi-frame image into a bitmap.
     *
     *
     * Repeated calls to this function should give the same results,
     * allowing the PixelRef to be immutable.
     *
     *
     * Bitmap specifies the description of the format (config, size)
     * expected by the caller.  This can simply be identical
     * to the info returned by getImageInfo().
     *
     *
     * This contract also allows the caller to specify
     * different output-configs, which the implementation can
     * decide to support or not.
     *
     *
     * A size that does not match getImageInfo() implies a request
     * to scale. If the generator cannot perform this scale,
     * it will throw an exception.
     *
     *
     * If the info contains a non-null ColorSpace, the codec
     * will perform the appropriate color space transformation.
     *
     *
     * If the caller passes in the ColorSpace that maps to the
     * ICC profile reported by getICCProfile(), the color space
     * transformation is a no-op.
     *
     *
     * If the caller passes a null SkColorSpace, no color space
     * transformation will be done.
     *
     * @param bitmap      the description of the format (config, size) expected by the caller
     * @param frame       index of the frame in multi-frame image to decode
     * @return            this
     */
    fun readPixels(bitmap: Bitmap?, frame: Int): Codec {
        return try {
            Stats.onNativeCall()
            _validateResult(
                Codec_nReadPixels(
                    _ptr,
                    getPtr(bitmap),
                    frame,
                    -1
                )
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(bitmap)
        }
    }

    /**
     *
     * Decodes a frame in a multi-frame image into a bitmap.
     *
     *
     * Repeated calls to this function should give the same results,
     * allowing the PixelRef to be immutable.
     *
     *
     * Bitmap specifies the description of the format (config, size)
     * expected by the caller.  This can simply be identical
     * to the info returned by getImageInfo().
     *
     *
     * This contract also allows the caller to specify
     * different output-configs, which the implementation can
     * decide to support or not.
     *
     *
     * A size that does not match getImageInfo() implies a request
     * to scale. If the generator cannot perform this scale,
     * it will throw an exception.
     *
     *
     * If the info contains a non-null ColorSpace, the codec
     * will perform the appropriate color space transformation.
     *
     *
     * If the caller passes in the ColorSpace that maps to the
     * ICC profile reported by getICCProfile(), the color space
     * transformation is a no-op.
     *
     *
     * If the caller passes a null SkColorSpace, no color space
     * transformation will be done.
     *
     * @param bitmap      the description of the format (config, size) expected by the caller
     * @param frame       index of the frame in multi-frame image to decode
     * @param priorFrame  index of the frame already in bitmap, might be used to optimize retrieving current frame
     * @return            this
     */
    fun readPixels(bitmap: Bitmap?, frame: Int, priorFrame: Int): Codec {
        return try {
            Stats.onNativeCall()
            _validateResult(
                Codec_nReadPixels(
                    _ptr,
                    getPtr(bitmap),
                    frame,
                    priorFrame
                )
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(bitmap)
        }
    }

    /**
     *
     * Return the number of frames in the image.
     *
     *
     * May require reading through the stream.
     */
    val frameCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetFrameCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Return info about a single frame.
     *
     *
     * Only supported by multi-frame images. Does not read through the stream,
     * so it should be called after getFrameCount() to parse any frames that
     * have not already been parsed.
     */
    fun getFrameInfo(frame: Int): AnimationFrameInfo {
        return try {
            Stats.onNativeCall()
            AnimationFrameInfo.fromInteropPointer { _nGetFrameInfo(_ptr, frame, it) }
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Return info about all the frames in the image.
     *
     *
     * May require reading through the stream to determine info about the
     * frames (including the count).
     *
     *
     * As such, future decoding calls may require a rewind.
     *
     *
     * For still (non-animated) image codecs, this will return an empty array.
     */
    val framesInfo: Array<AnimationFrameInfo>
        get() = try {
            Stats.onNativeCall()
            val buffer = _nGetFramesInfo(_ptr)
            val size = FramesInfo_nGetSize(buffer)
            if (size > 0) {
                AnimationFrameInfo.fromInteropArrayPointer(size) {
                    FramesInfo_nGetInfos(buffer, it)
                }
            } else {
                arrayOf()
            }.also {
                FramesInfo_nDelete(buffer)
            }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     *
     * Return the number of times to repeat, if this image is animated. This number does not
     * include the first play through of each frame. For example, a repetition count of 4 means
     * that each frame is played 5 times and then the animation stops.
     *
     *
     * It can return -1, a negative number, meaning that the animation
     * should loop forever.
     *
     *
     * May require reading the stream to find the repetition count.
     *
     *
     * As such, future decoding calls may require a rewind.
     *
     *
     * For still (non-animated) image codecs, this will return 0.
     */
    val repetitionCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetRepetitionCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    internal object _FinalizerHolder {
        val PTR = Codec_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetFinalizer")
private external fun Codec_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetImageInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetImageInfo")
private external fun Codec_nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Codec__1nReadPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nReadPixels")
private external fun Codec_nReadPixels(ptr: NativePointer, bitmapPtr: NativePointer, frame: Int, priorFrame: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nMakeFromData")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nMakeFromData")
private external fun _nMakeFromData(dataPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetSizeWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetSizeWidth")
private external fun _nGetSizeWidth(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetSizeHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetSizeHeight")
private external fun _nGetSizeHeight(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetEncodedOrigin")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetEncodedOrigin")
private external fun _nGetEncodedOrigin(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetEncodedImageFormat")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetEncodedImageFormat")
private external fun _nGetEncodedImageFormat(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFrameCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetFrameCount")
private external fun _nGetFrameCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFrameInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetFrameInfo")
private external fun _nGetFrameInfo(ptr: NativePointer, frame: Int, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFramesInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetFramesInfo")
private external fun _nGetFramesInfo(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetRepetitionCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nGetRepetitionCount")
private external fun _nGetRepetitionCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nFramesInfo_Delete")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nFramesInfo_Delete")
private external fun FramesInfo_nDelete(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Codec__1nFramesInfo_GetSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nFramesInfo_GetSize")
private external fun FramesInfo_nGetSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nFramesInfo_GetInfos")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Codec__1nFramesInfo_GetInfos")
private external fun FramesInfo_nGetInfos(ptr: NativePointer, result: InteropPointer)
