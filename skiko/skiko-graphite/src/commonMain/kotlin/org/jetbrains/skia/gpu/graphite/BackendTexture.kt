package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skiko.ExperimentalSkikoApi

@ExperimentalSkikoApi
class BackendTexture internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            GraphiteLibrary.load()
        }

        fun makeMetal(width: Int, height: Int, texturePtr: NativePointer): BackendTexture {
            requireMetalSupport()
            require(texturePtr != NullPointer) { "Metal texture pointer is null" }
            require(width > 0 && height > 0) { "Texture dimensions must be positive" }
            Stats.onNativeCall()
            return BackendTexture(_nMakeMetal(width, height, texturePtr))
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetBackendTextureFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_BackendTexture__1nGetFinalizer")
private external fun _nGetBackendTextureFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_BackendTexture__1nMakeMetal")
private external fun _nMakeMetal(width: Int, height: Int, texturePtr: NativePointer): NativePointer
