package org.jetbrains.skia.gpu.ganesh

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

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
fun Surface.Companion.makeFromBackendRenderTarget(
    context: DirectContext,
    rt: BackendRenderTarget,
    origin: SurfaceOrigin,
    colorFormat: SurfaceColorFormat,
    colorSpace: ColorSpace?,
    surfaceProps: SurfaceProps? = null,
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
                toInterop(surfaceProps?.packToIntArray()),
            )
        }
        if (ptr == NullPointer) null else Surface(ptr, arrayOf(context, rt))
    } finally {
        reachabilityBarrier(context)
        reachabilityBarrier(rt)
        reachabilityBarrier(colorSpace)
    }
}

fun Surface.Companion.makeFromMTKView(
    context: DirectContext,
    mtkViewPtr: NativePointer,
    origin: SurfaceOrigin,
    sampleCount: Int,
    colorFormat: SurfaceColorFormat,
    colorSpace: ColorSpace?,
    surfaceProps: SurfaceProps?,
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
                toInterop(surfaceProps?.packToIntArray()),
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
fun Surface.Companion.makeRenderTarget(
    context: DirectContext,
    budgeted: Boolean,
    imageInfo: ImageInfo,
): Surface = makeRenderTarget(context, budgeted, imageInfo, 0, SurfaceOrigin.BOTTOM_LEFT, null, false)

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
fun Surface.Companion.makeRenderTarget(
    context: DirectContext,
    budgeted: Boolean,
    imageInfo: ImageInfo,
    sampleCount: Int,
    surfaceProps: SurfaceProps?,
): Surface = makeRenderTarget(
    context,
    budgeted,
    imageInfo,
    sampleCount,
    SurfaceOrigin.BOTTOM_LEFT,
    surfaceProps,
    false,
)

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
fun Surface.Companion.makeRenderTarget(
    context: DirectContext,
    budgeted: Boolean,
    imageInfo: ImageInfo,
    sampleCount: Int,
    origin: SurfaceOrigin,
    surfaceProps: SurfaceProps?,
): Surface = makeRenderTarget(context, budgeted, imageInfo, sampleCount, origin, surfaceProps, false)

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
fun Surface.Companion.makeRenderTarget(
    context: DirectContext,
    budgeted: Boolean,
    imageInfo: ImageInfo,
    sampleCount: Int,
    origin: SurfaceOrigin,
    surfaceProps: SurfaceProps?,
    shouldCreateWithMips: Boolean,
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
                shouldCreateWithMips,
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

@ExternalSymbolName("org_jetbrains_skia_gpu_ganesh_SurfaceFactory__1nMakeFromBackendRenderTarget")
private external fun _nMakeFromBackendRenderTarget(
    contextPtr: NativePointer,
    backendRenderTargetPtr: NativePointer,
    surfaceOrigin: Int,
    colorType: Int,
    colorSpacePtr: NativePointer,
    surfaceProps: InteropPointer,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_ganesh_SurfaceFactory__1nMakeFromMTKView")
private external fun _nMakeFromMTKView(
    contextPtr: NativePointer,
    mtkViewPtr: NativePointer,
    surfaceOrigin: Int,
    sampleCount: Int,
    colorType: Int,
    colorSpacePtr: NativePointer,
    surfaceProps: InteropPointer,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_ganesh_SurfaceFactory__1nMakeRenderTarget")
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
    shouldCreateWithMips: Boolean,
): NativePointer
