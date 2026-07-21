package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi

@ExperimentalSkikoApi
class BackendTexture internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            GraphiteLibrary.load()
        }

        fun wrapMetalTexture(texturePtr: NativePointer, width: Int, height: Int): BackendTexture {
            requireMetalSupport()
            require(texturePtr != NullPointer) { "Metal texture pointer is null" }
            require(width > 0 && height > 0) { "Texture dimensions must be positive" }
            return BackendTexture(_nWrapMetalTexture(texturePtr, width, height))
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetBackendTextureFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_BackendTexture__1nGetFinalizer")
private external fun _nGetBackendTextureFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_BackendTexture__1nWrapMetalTexture")
private external fun _nWrapMetalTexture(texturePtr: NativePointer, width: Int, height: Int): NativePointer
