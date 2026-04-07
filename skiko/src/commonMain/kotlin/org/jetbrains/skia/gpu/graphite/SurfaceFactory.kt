package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skiko.ExperimentalSkikoApi

@ExperimentalSkikoApi
fun Surface.Companion.makeFromBackendTexture(
    recorder: Recorder,
    backendTexture: BackendTexture,
    colorFormat: SurfaceColorFormat,
    colorSpace: ColorSpace?,
    surfaceProps: SurfaceProps? = null
): Surface? {
    return try {
        Stats.onNativeCall()
        val ptr = interopScope {
            _nMakeFromBackendTexture(
                recorder._ptr,
                backendTexture._ptr,
                colorFormat.ordinal,
                getPtr(colorSpace),
                toInterop(surfaceProps?.packToIntArray())
            )
        }
        if (ptr == NullPointer)
            null
        else
            Surface(ptr)
    } finally {
        reachabilityBarrier(recorder)
        reachabilityBarrier(backendTexture)
        reachabilityBarrier(colorSpace)
    }
}


@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_SurfaceFactory__1nMakeFromBackendTexture")
private external fun _nMakeFromBackendTexture(
    recorderPtr: NativePointer,
    backendTexturePtr: NativePointer,
    colorType: Int,
    colorSpacePtr: NativePointer,
    surfaceProps: InteropPointer
): NativePointer
