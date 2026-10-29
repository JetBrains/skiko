package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import kotlin.jvm.JvmStatic

class Codec internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR), IHasImageInfo {
    companion object {
        /**
         * If this data represents an encoded image that we know how to decode,
         * return an Codec that can decode it. Otherwise throws IllegalArgumentException.
         */
        @JvmStatic
        fun makeFromData(data: Data?): Codec = nativeCallFromStatic(data) { data ->
            val ptr = _nMakeFromData(data.pointerOrNullPointer)
            require(ptr != NullPointer) { "Unsupported format" }
            Codec(ptr)
        }

        @JvmStatic
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
        get() = nativeCall { IPoint(_nGetSizeWidth(_ptr), _nGetSizeHeight(_ptr)) }
    val encodedOrigin: EncodedOrigin
        get() = nativeCall { EncodedOrigin.entries[_nGetEncodedOrigin(_ptr)] }
    val encodedImageFormat: EncodedImageFormat
        get() = nativeCall { EncodedImageFormat.entries[_nGetEncodedImageFormat(_ptr)] }

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
    fun readPixels(bitmap: Bitmap?): Codec = nativeCall(bitmap) { bitmap ->
        _validateResult(
            Codec_nReadPixels(
                _ptr,
                bitmap.pointerOrNullPointer,
                0,
                -1
            )
        )
        this
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
    fun readPixels(bitmap: Bitmap?, frame: Int): Codec = nativeCall(bitmap) {bitmap ->
        _validateResult(
            Codec_nReadPixels(
                _ptr,
                bitmap.pointerOrNullPointer,
                frame,
                -1
            )
        )
        this
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
    fun readPixels(bitmap: Bitmap?, frame: Int, priorFrame: Int): Codec = nativeCall(bitmap) { bitmap ->
        _validateResult(
            Codec_nReadPixels(
                _ptr,
                bitmap.pointerOrNullPointer,
                frame,
                priorFrame
            )
        )
        this
    }

    /**
     *
     * Return the number of frames in the image.
     *
     *
     * May require reading through the stream.
     */
    val frameCount: Int
        get() = nativeCall { _nGetFrameCount(_ptr) }

    /**
     *
     * Return info about a single frame.
     *
     *
     * Only supported by multi-frame images. Does not read through the stream,
     * so it should be called after getFrameCount() to parse any frames that
     * have not already been parsed.
     */
    fun getFrameInfo(frame: Int): AnimationFrameInfo = nativeCall {
        AnimationFrameInfo.fromInteropPointer { _nGetFrameInfo(_ptr, frame, it) }
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
        get() = nativeCall {
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
        get() = nativeCall { _nGetRepetitionCount(_ptr) }

    internal object _FinalizerHolder {
        val PTR = Codec_nGetFinalizer()
    }
}

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of a delete function without calling it, so it is short and cannot
// block.
// Native call path: _nGetFinalizer
private external fun Codec_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetImageInfo")
// Needs ForceNativeThreadState: builds a color space from the embedded ICC profile, walking its curve tables, so its
// time grows with the profile.
// Native call path: SkCodec::getInfo -> SkEncodedInfo::makeImageInfo -> SkColorSpace::Make
@ForceNativeThreadState
private external fun Codec_nGetImageInfo(ptr: NativePointer, imageInfo: InteropPointer, colorSpacePtrs: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Codec__1nReadPixels")
// Needs ForceNativeThreadState: decodes the whole image from the stream, so its time grows with the image size.
// Native call path: SkCodec::getPixels -> onGetPixels
@ForceNativeThreadState
private external fun Codec_nReadPixels(ptr: NativePointer, bitmapPtr: NativePointer, frame: Int, priorFrame: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nMakeFromData")
// Needs ForceNativeThreadState: tries each image decoder on the encoded data, scanning headers and markers, so its time
// grows with the data size.
// Native call path: SkCodec::MakeFromData -> SkCodec::MakeFromStream
@ForceNativeThreadState
private external fun _nMakeFromData(dataPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetSizeWidth")
// No ForceNativeThreadState: returns the stored image width, so it is short and cannot block.
// Native call path: SkCodec::dimensions
private external fun _nGetSizeWidth(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetSizeHeight")
// No ForceNativeThreadState: returns the stored image height, so it is short and cannot block.
// Native call path: SkCodec::dimensions
private external fun _nGetSizeHeight(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetEncodedOrigin")
// No ForceNativeThreadState: returns the stored origin value, so it is short and cannot block.
// Native call path: SkCodec::getOrigin
private external fun _nGetEncodedOrigin(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetEncodedImageFormat")
// No ForceNativeThreadState: returns the encoded format from a constant or a stored field, so it is short and cannot
// block.
// Native call path: SkCodec::getEncodedFormat -> onGetEncodedFormat
private external fun _nGetEncodedImageFormat(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFrameCount")
// Needs ForceNativeThreadState: walks the stream and decodes the config of every frame, so its time grows with the
// frame count.
// Native call path: SkCodec::getFrameCount -> SkWuffsCodec::onGetFrameCountInternal
@ForceNativeThreadState
private external fun _nGetFrameCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFrameInfo")
// Needs ForceNativeThreadState: builds a color space from the ICC profile and scans its curve tables, so its time grows
// with the profile.
// Native call path: SkBmpRustCodec::onGetFrameInfo -> SkCodec::getInfo -> skcms_MaxRoundtripError
@ForceNativeThreadState
private external fun _nGetFrameInfo(ptr: NativePointer, frame: Int, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetFramesInfo")
// Needs ForceNativeThreadState: scans every frame in the stream and builds a list with one entry per frame, so its time
// grows with the frame count.
// Native call path: SkCodec::getFrameInfo -> getFrameCount
@ForceNativeThreadState
private external fun _nGetFramesInfo(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Codec__1nGetRepetitionCount")
// No ForceNativeThreadState: reads a stored loop count from the image header, so it is short and cannot block.
// Native call path: SkCodec::getRepetitionCount -> onGetRepetitionCount
private external fun _nGetRepetitionCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nFramesInfo_Delete")
// No ForceNativeThreadState: frees a list of frame records that hold only plain numbers, so it is short and cannot
// block.
// Native call path: std::vector<SkCodec::FrameInfo>::~vector
private external fun FramesInfo_nDelete(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Codec__1nFramesInfo_GetSize")
// No ForceNativeThreadState: returns the stored size of the frame list, so it is short and cannot block.
// Native call path: std::vector::size
private external fun FramesInfo_nGetSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Codec__1nFramesInfo_GetInfos")
// Needs ForceNativeThreadState: writes 11 ints for every frame in the list, so its time grows with the frame count.
// Native call path: AnimationFrameInfo::copyToInterop -> copyToInteropAtIndex
@ForceNativeThreadState
private external fun FramesInfo_nGetInfos(ptr: NativePointer, result: InteropPointer)
// </editor-fold>