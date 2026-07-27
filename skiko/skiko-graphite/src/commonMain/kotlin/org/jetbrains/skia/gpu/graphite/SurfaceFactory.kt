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
