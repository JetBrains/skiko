package org.jetbrains.skia.gpu.ganesh

import org.jetbrains.skia.BackendTexture
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
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
 * Copies a rectangle of pixels from this image to [dst]. Copy starts at ([srcX], [srcY]) and
 * does not exceed the image's width and height.
 *
 * The bitmap specifies the destination dimensions, color type, alpha type, color space,
 * pixel storage, and row bytes. Returns false if its pixel storage is null, its row bytes
 * are less than the minimum required for its image info, or pixels cannot be accessed.
 *
 * Pixels are copied only if pixel conversion is possible. If the image's color type is
 * [ColorType.GRAY_8] or [ColorType.ALPHA_8], the destination color type must match.
 * If the image's color type is [ColorType.GRAY_8], the destination color space must match.
 * If the image's alpha type is [ColorAlphaType.OPAQUE], the destination alpha type must match.
 * If the image has no color space, the destination must also have no color space.
 *
 * [srcX] and [srcY] may be negative to copy only the top or left of the source. Returns false
 * if the image's width or height is zero or negative, if `abs(srcX) >= width`, or if
 * `abs(srcY) >= height`.
 *
 * If [cache] is true, pixels may be retained locally. Otherwise, pixels are not added to
 * the local cache.
 *
 * @param context GPU context used to read the image's pixels
 * @param dst destination bitmap with allocated pixel storage
 * @param srcX column index whose absolute value is less than the image's width
 * @param srcY row index whose absolute value is less than the image's height
 * @param cache whether the pixels may be cached locally
 * @return true if pixels are copied to [dst]
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
// Needs ForceNativeThreadState: wraps a Metal GPU texture and reads its properties through Metal.
// Native call path: SkImages::AdoptTextureFrom -> wrapBackendTexture -> GrMtlGpu::onWrapBackendTexture
@ForceNativeThreadState
private external fun _nAdoptTextureFrom(
    contextPtr: NativePointer,
    backendTexturePtr: NativePointer,
    surfaceOrigin: Int,
    colorType: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_ganesh_ImageFactory__1nAdoptTextureFromAlphaType")
// Needs ForceNativeThreadState: wraps a Metal GPU texture and reads its properties through Metal.
// Native call path: SkImages::AdoptTextureFrom -> wrapBackendTexture -> GrMtlGpu::onWrapBackendTexture
@ForceNativeThreadState
private external fun _nAdoptTextureFromAlphaType(
    contextPtr: NativePointer,
    backendTexturePtr: NativePointer,
    surfaceOrigin: Int,
    colorType: Int,
    alphaType: Int,
): NativePointer
