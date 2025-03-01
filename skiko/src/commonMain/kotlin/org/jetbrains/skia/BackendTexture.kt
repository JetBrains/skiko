package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skiko.RenderException

class BackendTexture internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        /**
         * Creates BackendTexture from GL texture.
         *
         * @param textureFormat - GL enum, must be valid
         * @throws RuntimeException if nullptr is returned.
         *
         * @see glTextureParametersModified
         */
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

    /**
     * Call this to indicate to DirectContext that the texture parameters have been modified in the GL context externally
     */
    fun glTextureParametersModified() {
        return try {
            Stats.onNativeCall()
            _nGLTextureParametersModified(getPtr(this))
        } finally {
            reachabilityBarrier(this)
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

@ExternalSymbolName("org_jetbrains_skia_BackendTexture__1nGLTextureParametersModified")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BackendTexture__1nGLTextureParametersModified")
private external fun _nGLTextureParametersModified(backendTexturePtr: NativePointer)
