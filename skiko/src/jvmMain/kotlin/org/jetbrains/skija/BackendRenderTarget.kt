package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Stats

class BackendRenderTarget internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
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
            return BackendRenderTarget(_nMakeGL(width, height, sampleCnt, stencilBits, fbId, fbFormat))
        }

        fun makeMetal(width: Int, height: Int, texturePtr: Long): BackendRenderTarget {
            Stats.onNativeCall()
            return BackendRenderTarget(_nMakeMetal(width, height, texturePtr))
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
            texturePtr: Long,
            format: Int,
            sampleCnt: Int,
            levelCnt: Int
        ): BackendRenderTarget {
            Stats.onNativeCall()
            return BackendRenderTarget(_nMakeDirect3D(width, height, texturePtr, format, sampleCnt, levelCnt))
        }

        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMakeGL(width: Int, height: Int, sampleCnt: Int, stencilBits: Int, fbId: Int, fbFormat: Int): Long
        @JvmStatic external fun _nMakeMetal(width: Int, height: Int, texturePtr: Long): Long
        @JvmStatic external fun _nMakeDirect3D(
            width: Int,
            height: Int,
            texturePtr: Long,
            format: Int,
            sampleCnt: Int,
            levelCnt: Int
        ): Long

        init {
            staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}