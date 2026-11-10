package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Native.Companion.NullPointer
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
): Surface? = nativeCallFromStatic(recorder, backendTexture, colorSpace) { recorder, backendTexture, colorSpace ->
    val ptr = interopScope {
        _nWrapBackendTexture(
            recorder.nativePtr,
            backendTexture.nativePtr,
            colorSpace.pointerOrNullPointer,
            toInterop(surfaceProps?.packToIntArray()),
        )
    }
    if (ptr == NullPointer) null else Surface(ptr)
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_SurfaceFactory__1nWrapBackendTexture")
// Needs ForceNativeThreadState: wraps a Metal texture in a new surface and drops the old color space and surface
// references.
// Native call path: SkSurfaces::WrapBackendTexture -> MtlResourceProvider::createWrappedTexture -> Device::Make
@ForceNativeThreadState
private external fun _nWrapBackendTexture(
    recorderPtr: NativePointer,
    backendTexturePtr: NativePointer,
    colorSpacePtr: NativePointer,
    surfaceProps: InteropPointer,
): NativePointer
