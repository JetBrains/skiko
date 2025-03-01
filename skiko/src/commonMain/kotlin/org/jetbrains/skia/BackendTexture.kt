package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skiko.RenderException

class BackendTexture internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun makeGL(
            textureId: Int,
            textureTarget: Int,
            textureFormat: Int,
            width: Int,
            height: Int,
            isMipmapped: Boolean
        ): BackendTexture {
            Stats.onNativeCall()
            val ptr = _nMakeGL(
                textureId,
                textureTarget,
                textureFormat,
                width,
                height,
                isMipmapped
            )
            return if (ptr == NullPointer) throw RenderException("Can't create OpenGL BackendTexture")
            else BackendTexture(ptr)
        }

        init {
            staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = BackendTexture_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_BackendTexture__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BackendTexture__1nGetFinalizer")
private external fun BackendTexture_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BackendTexture__1nMakeGL")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BackendTexture__1nMakeGL")
private external fun _nMakeGL(
    textureId: Int,
    target: Int,
    format: Int,
    width: Int,
    height: Int,
    isMipmapped: Boolean
): NativePointer
