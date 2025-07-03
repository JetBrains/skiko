package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skiko.RenderException

class BackendRenderTarget internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun makeGL(
            width: Int,
            height: Int,
            sampleCnt: Int,
            stencilBits: Int,
            fbId: Int,
            fbFormat: Int
        ): BackendRenderTarget {
            Stats.onNativeCall()
            val ptr = BackendRenderTarget_nMakeGL(width, height, sampleCnt, stencilBits, fbId, fbFormat)
            if (ptr == NullPointer) throw RenderException("Can't create OpenGL BackendRenderTarget")
            return BackendRenderTarget(ptr)
        }

        fun makeMetal(width: Int, height: Int, texturePtr: NativePointer): BackendRenderTarget {
            Stats.onNativeCall()
            return BackendRenderTarget(BackendRenderTarget_nMakeMetal(width, height, texturePtr))
        }

        /**
         *
         * Creates Direct3D backend render target object from D3D12 texture resource.
         *
         * For more information refer to skia GrBackendRenderTarget class.
         *
         * @param width         width of the render target in pixels
         * @param height        height of the render target in pixels
         * @param texturePtr    pointer to ID3D12Resource texture resource object; must be not zero
         * @param format        pixel data DXGI_FORMAT fromat of the texturePtr resource
         * @param sampleCnt     samples count for texture resource
         * @param levelCnt      sampling quality level for texture resource
         */
        fun makeDirect3D(
            width: Int,
            height: Int,
            texturePtr: NativePointer,
            format: Int,
            sampleCnt: Int,
            levelCnt: Int
        ): BackendRenderTarget {
            Stats.onNativeCall()
            return BackendRenderTarget(
                BackendRenderTarget_nMakeDirect3D(
                    width,
                    height,
                    texturePtr,
                    format,
                    sampleCnt,
                    levelCnt
                )
            )
        }

        init {
            staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = BackendRenderTarget_nGetFinalizer()
    }
}