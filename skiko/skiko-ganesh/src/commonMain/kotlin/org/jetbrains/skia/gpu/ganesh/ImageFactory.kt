package org.jetbrains.skia.gpu.ganesh

import org.jetbrains.skia.BackendTexture
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.Image
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

/**
 * Creates GPU-backed SkImage from backendTexture associated with context.
 *
 * Skia will assume ownership of the resource and will release it when no longer needed.
 * A non-null Image is returned if format of backendTexture is recognized and supported.
 * Recognized formats vary by GPU backend.
 *
 * @param context         GPU context
 * @param backendTexture  texture residing on GPU
 * @param origin          origin of backendTexture
 * @param colorType       color type of the resulting Image
 * @return                created Image
 *
 * @throws RuntimeException - if nullPtr is returned.
 */
fun Image.Companion.adoptTextureFrom(
    context: DirectContext,
    backendTexture: BackendTexture,
    origin: SurfaceOrigin,
    colorType: ColorType,
): Image = adoptTextureFrom(context, backendTexture, origin, colorType, null)

/**
 * Creates GPU-backed SkImage from backendTexture associated with context.
 *
 * Skia will assume ownership of the resource and will release it when no longer needed.
 * A non-null Image is returned if format of backendTexture is recognized and supported.
 * Recognized formats vary by GPU backend.
 *
 * @param context         GPU context
 * @param backendTexture  texture residing on GPU
 * @param origin          origin of backendTexture
 * @param colorType       color type of the resulting Image
 * @param alphaType       alpha type of the resulting Image
 * @return                created Image
 *
 * @throws RuntimeException - if nullPtr is returned.
 */
fun Image.Companion.adoptTextureFrom(
    context: DirectContext,
    backendTexture: BackendTexture,
    origin: SurfaceOrigin,
    colorType: ColorType,
    alphaType: ColorAlphaType?,
): Image {
    return try {
        Stats.onNativeCall()
        val ptr = if (alphaType == null) {
            _nAdoptTextureFrom(getPtr(context), getPtr(backendTexture), origin.ordinal, colorType.ordinal)
        } else {
            _nAdoptTextureFromAlphaType(
                getPtr(context),
                getPtr(backendTexture),
                origin.ordinal,
                colorType.ordinal,
                alphaType.ordinal,
            )
        }
        require(ptr != NullPointer) { "Failed to Image::makeFromTexture" }
        Image(ptr)
    } finally {
        reachabilityBarrier(context)
        reachabilityBarrier(backendTexture)
    }
}

fun Image.readPixels(context: DirectContext, dst: Bitmap): Boolean =
    readPixels(context, dst, 0, 0, false)

fun Image.readPixels(context: DirectContext, dst: Bitmap, srcX: Int, srcY: Int): Boolean =
    readPixels(context, dst, srcX, srcY, false)

/**
 *
 * Copies Rect of pixels from Image to Bitmap. Copy starts at offset (srcX, srcY),
 * and does not exceed Image (width, height).
 *
 *
 * Image ColorType and Bitmap ColorType must match, or be a combination of
 * ColorType.RGBA_8888 and ColorType.BGRA_8888.
 * Image ColorSpace and Bitmap ColorSpace must match. Image AlphaType and
 * Bitmap AlphaType must match, or be a combination of AlphaType.PREMUL and
 * AlphaType.UNPREMUL. If Bitmap pixels are unallocated, Bitmap row bytes must be zero.
 *
 *
 * srcX and srcY may be negative to copy only top or left of source. Returns false if
 * Bitmap pixels could not be allocated or pixel conversion is not possible.
 *
 *
 * If Bitmap pixels are unallocated, Bitmap is resized to fit Image width and height.
 * If Bitmap pixels are allocated, only pixels fitting both Image and Bitmap are copied.
 * Bitmap pixel address does not change unless pixels are reallocated.
 *
 *
 * On success, pixels are copied to Bitmap and true is returned.
 *
 *
 * Returns false if Image is texture-backed and context is null.
 *
 *
 * Returns false if srcX &lt; 0 and -srcX is equal to or greater than Bitmap width.
 *
 *
 * Returns false if srcY &lt; 0 and -srcY is equal to or greater than Bitmap height.
 *
 *
 * Returns false if srcX is equal to or greater than Image width.
 *
 *
 * Returns false if srcY is equal to or greater than Image height.
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
fun Image.readPixels(
    context: DirectContext,
    dst: Bitmap,
    srcX: Int,
    srcY: Int,
    cache: Boolean,
): Boolean {
    return try {
        _nReadPixelsBitmapWithContext(
            getPtr(this),
            getPtr(context),
            getPtr(dst),
            srcX,
            srcY,
            cache,
        )
    } finally {
        reachabilityBarrier(this)
        reachabilityBarrier(context)
        reachabilityBarrier(dst)
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_ganesh_ImageFactory__1nReadPixelsBitmapWithContext")
private external fun _nReadPixelsBitmapWithContext(
    imagePtr: NativePointer,
    contextPtr: NativePointer,
    bitmapPtr: NativePointer,
    srcX: Int,
    srcY: Int,
    cache: Boolean,
): Boolean

@ExternalSymbolName("org_jetbrains_skia_gpu_ganesh_ImageFactory__1nAdoptTextureFrom")
private external fun _nAdoptTextureFrom(
    contextPtr: NativePointer,
    backendTexturePtr: NativePointer,
    surfaceOrigin: Int,
    colorType: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_ganesh_ImageFactory__1nAdoptTextureFromAlphaType")
private external fun _nAdoptTextureFromAlphaType(
    contextPtr: NativePointer,
    backendTexturePtr: NativePointer,
    surfaceOrigin: Int,
    colorType: Int,
    alphaType: Int,
): NativePointer
