package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skiko.ExperimentalSkikoApi

/**
 * Creates a Graphite surface that renders into a [backendTexture] using [recorder].
 *
 * @param recorder recorder used by the surface to record drawing commands.
 * @param backendTexture backend texture that receives the rendered content.
 * @param colorSpace color space describing how colors are interpreted, or `null` for no color space.
 * @param surfaceProps optional surface properties controlling pixel geometry and rendering behavior.
 * @return a surface wrapping the backend texture, or `null` if the surface could not be created.
 */
@ExperimentalSkikoApi
fun Surface.Companion.wrapBackendTexture(
    recorder: Recorder,
    backendTexture: BackendTexture,
    colorSpace: ColorSpace?,
    surfaceProps: SurfaceProps? = null,
): Surface? {
    return try {
        Stats.onNativeCall()
        val ptr = interopScope {
            _nWrapBackendTexture(
                recorder.nativePtr,
                backendTexture.nativePtr,
                getPtr(colorSpace),
                toInterop(surfaceProps?.packToIntArray()),
            )
        }
        if (ptr == NullPointer) null else Surface(ptr)
    } finally {
        reachabilityBarrier(recorder)
        reachabilityBarrier(backendTexture)
        reachabilityBarrier(colorSpace)
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_SurfaceFactory__1nWrapBackendTexture")
private external fun _nWrapBackendTexture(
    recorderPtr: NativePointer,
    backendTexturePtr: NativePointer,
    colorSpacePtr: NativePointer,
    surfaceProps: InteropPointer,
): NativePointer
