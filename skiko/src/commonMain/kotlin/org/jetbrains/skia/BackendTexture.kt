package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class BackendTexture internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        /**
         * Creates BackendTexture from GL texture.
         *
         * @param width - width of the [BackendTexture] to be created
         * @param height - height of the [BackendTexture] to be created
         * @param isMipmapped - if the passed [textureId] has a GL mipmap, this should be true, otherwise false
         * @param textureId - GL id of the texture to use
         * @param textureTarget - GL enum, must be valid texture target for 2D, e.g. GL_TEXTURE_2D
         * @param textureFormat - GL enum, must be valid color format, e.g. GL_RGBA or GL_BGRA_INTEGER
         *
         * @see glTextureParametersModified
         */
        fun makeGL(
            width: Int,
            height: Int,
            isMipmapped: Boolean,
            textureId: Int,
            textureTarget: Int,
            textureFormat: Int
        ): BackendTexture {
            Stats.onNativeCall()
            val ptr = _nMakeGL(
                width,
                height,
                isMipmapped,
                textureId,
                textureTarget,
                textureFormat
            )
            return BackendTexture(ptr)
        }

        /**
         * The default `D3D12_RESOURCE_STATE_COMMON` resource state (`0`) used by [makeDirect3D] when the
         * caller does not specify one. See
         * [D3D12_RESOURCE_STATES](https://learn.microsoft.com/windows/win32/api/d3d12/ne-d3d12-d3d12_resource_states).
         */
        const val D3D12_RESOURCE_STATE_COMMON: Int = 0

        /**
         * Wraps an already-allocated Metal texture (`id<MTLTexture>`) as a [BackendTexture] so it can be
         * sampled by Skia with no CPU round-trip.
         *
         * The texture **must** live on the same `MTLDevice` as the [DirectContext] it will be used with — read
         * that device from a render context's Metal handle accessor
         * (`RenderContext.metalDevice` / `RenderContext.metalDevicePointer`). Pair the result with
         * [Image.adoptTextureFrom] to obtain a GPU-backed [Image].
         *
         * **Ownership.** The native side takes a **retain** on the underlying `id<MTLTexture>` (Skia's
         * `retain`/`kAdopt` semantics on `GrMtlTextureInfo`): the texture stays alive for as long as this
         * [BackendTexture] (and any [Image] adopted from it) is alive, and its retain is released when this
         * object is [closed][close]. You keep ownership of your own reference — release it on your side as you
         * normally would; do not expect this call to consume it. Callers remain responsible for not mutating
         * the texture's contents while Skia may be sampling it.
         *
         * Available only on Metal-enabled builds; on a build without Metal this throws
         * [UnsupportedOperationException].
         *
         * @param width         width of the texture, in pixels; must be positive
         * @param height        height of the texture, in pixels; must be positive
         * @param mtlTexturePtr native pointer to the `id<MTLTexture>` (e.g. `objcPtr()` on Kotlin/Native, or a
         *                      `__bridge`-cast address on the JVM); must not be [NullPointer]
         * @param isMipmapped   whether [mtlTexturePtr] has a complete mipmap chain
         * @throws IllegalArgumentException if [mtlTexturePtr] is [NullPointer] or [width]/[height] is not positive
         * @throws UnsupportedOperationException if this Skiko build has no Metal backend
         */
        fun makeMetal(
            width: Int,
            height: Int,
            mtlTexturePtr: NativePointer,
            isMipmapped: Boolean = false
        ): BackendTexture {
            require(mtlTexturePtr != NullPointer) { "mtlTexturePtr must not be null" }
            require(width > 0) { "width must be positive, was $width" }
            require(height > 0) { "height must be positive, was $height" }
            Stats.onNativeCall()
            val ptr = _nMakeMetal(width, height, mtlTexturePtr, isMipmapped)
            if (ptr == NullPointer) throw UnsupportedOperationException(
                "Metal is not available in this Skiko build"
            )
            return BackendTexture(ptr)
        }

        /**
         * Wraps an already-allocated Direct3D 12 texture (`ID3D12Resource`) as a [BackendTexture] so it can be
         * sampled by Skia with no CPU round-trip.
         *
         * The resource **must** belong to the same `ID3D12Device` as the [DirectContext] it will be used with —
         * read that device from a render context's Direct3D handle accessors
         * (`RenderContext.direct3DDevicePointer` etc.). Pair the result with [Image.adoptTextureFrom] to obtain
         * a GPU-backed [Image].
         *
         * **Resource state.** Direct3D 12 makes resource-state tracking a client obligation, so you must tell
         * Skia the state the resource is currently in via [resourceState] (a `D3D12_RESOURCE_STATES` value).
         * It defaults to [D3D12_RESOURCE_STATE_COMMON]; pass the actual state (e.g.
         * `D3D12_RESOURCE_STATE_PIXEL_SHADER_RESOURCE` / `..._RENDER_TARGET`) when your resource is not in the
         * common state, otherwise Skia may issue an incorrect resource-state transition barrier.
         *
         * **Ownership.** The native side takes a **retain** on the underlying `ID3D12Resource` (Skia's
         * `retain`/`kAdopt` semantics on `GrD3DTextureResourceInfo`): the resource stays alive for as long as
         * this [BackendTexture] (and any [Image] adopted from it) is alive, and its retain is released when this
         * object is [closed][close]. You keep ownership of your own reference. Callers remain responsible for
         * not mutating the resource's contents while Skia may be sampling it.
         *
         * Available only on Direct3D-enabled builds; on a build without Direct3D this throws
         * [UnsupportedOperationException].
         *
         * @param width         width of the texture, in pixels; must be positive
         * @param height        height of the texture, in pixels; must be positive
         * @param texturePtr    native pointer to the `ID3D12Resource`; must not be [NullPointer]
         * @param format        the resource's `DXGI_FORMAT`
         * @param resourceState the resource's current `D3D12_RESOURCE_STATES`; defaults to
         *                      [D3D12_RESOURCE_STATE_COMMON]
         * @param sampleCnt     sample count of the resource
         * @param levelCnt      mip level count of the resource
         * @throws IllegalArgumentException if [texturePtr] is [NullPointer] or [width]/[height] is not positive
         * @throws UnsupportedOperationException if this Skiko build has no Direct3D backend
         */
        fun makeDirect3D(
            width: Int,
            height: Int,
            texturePtr: NativePointer,
            format: Int,
            resourceState: Int = D3D12_RESOURCE_STATE_COMMON,
            sampleCnt: Int = 1,
            levelCnt: Int = 1
        ): BackendTexture {
            require(texturePtr != NullPointer) { "texturePtr must not be null" }
            require(width > 0) { "width must be positive, was $width" }
            require(height > 0) { "height must be positive, was $height" }
            Stats.onNativeCall()
            val ptr = _nMakeDirect3DTexture(width, height, texturePtr, format, resourceState, sampleCnt, levelCnt)
            if (ptr == NullPointer) throw UnsupportedOperationException(
                "Direct3D is not available in this Skiko build"
            )
            return BackendTexture(ptr)
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
private external fun BackendTexture_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BackendTexture__1nMakeGL")
private external fun _nMakeGL(
    width: Int,
    height: Int,
    isMipmapped: Boolean,
    textureId: Int,
    target: Int,
    format: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BackendTexture__1nMakeMetal")
private external fun _nMakeMetal(
    width: Int,
    height: Int,
    texturePtr: NativePointer,
    isMipmapped: Boolean
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BackendTexture__1nMakeDirect3DTexture")
private external fun _nMakeDirect3DTexture(
    width: Int,
    height: Int,
    texturePtr: NativePointer,
    format: Int,
    resourceState: Int,
    sampleCnt: Int,
    levelCnt: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BackendTexture__1nGLTextureParametersModified")
private external fun _nGLTextureParametersModified(backendTexturePtr: NativePointer)
