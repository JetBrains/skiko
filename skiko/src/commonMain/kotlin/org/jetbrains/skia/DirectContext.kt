package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class DirectContext internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        fun makeGL(): DirectContext {
            Stats.onNativeCall()
            return DirectContext(_nMakeGL())
        }

        fun makeMetal(devicePtr: NativePointer, queuePtr: NativePointer): DirectContext {
            Stats.onNativeCall()
            return DirectContext(_nMakeMetal(devicePtr, queuePtr))
        }

        /**
         *
         * Creates Direct3D direct rendering context from D3D12 native objects.
         *
         * For more information refer to skia GrDirectContext class.
         *
         * @param adapterPtr    pointer to IDXGIAdapter1 object; must be not zero
         * @param devicePtr     pointer to ID3D12Device object, which is created with
         * provided adapter in adapterPtr; must be not zero
         * @param queuePtr      Pointer to ID3D12CommandQueue object, which
         * is created with provided device in devicePtr with
         * type D3D12_COMMAND_LIST_TYPE_DIRECT; must be not zero
         */
        fun makeDirect3D(adapterPtr: NativePointer, devicePtr: NativePointer, queuePtr: NativePointer): DirectContext {
            Stats.onNativeCall()
            return DirectContext(_nMakeDirect3D(adapterPtr, devicePtr, queuePtr))
        }

        init {
            staticLoad()
        }
    }

    fun flush(): DirectContext {
        Stats.onNativeCall()
        DirectContext_nFlush(_ptr)
        return this
    }

    fun resetAll(): DirectContext {
        Stats.onNativeCall()
        _nReset(_ptr, -1)
        return this
    }

    fun resetGLAll(): DirectContext {
        Stats.onNativeCall()
        _nReset(_ptr, 0xffff)
        return this
    }

    fun resetGL(vararg states: GLBackendState): DirectContext {
        Stats.onNativeCall()
        var flags = 0
        for (state in states) flags = flags or state._bit
        _nReset(_ptr, flags)
        return this
    }

    /**
     *
     * Submit outstanding work to the gpu from all previously un-submitted flushes.
     *
     * If the syncCpu flag is true this function will return once the gpu has finished with all submitted work.
     *
     * For more information refer to skia GrDirectContext::submit(bool syncCpu) method.
     *
     * @param syncCpu flag to sync cpu and gpu work submission
     */
    fun submit(syncCpu: Boolean) {
        Stats.onNativeCall()
        _nSubmit(_ptr, syncCpu)
    }

    /**
     *
     * Abandons all GPU resources and assumes the underlying backend 3D API context is no longer
     * usable. Call this if you have lost the associated GPU context, and thus internal texture,
     * buffer, etc. references/IDs are now invalid. Calling this ensures that the destructors of the
     * context and any of its created resource objects will not make backend 3D API calls. Content
     * rendered but not previously flushed may be lost. After this function is called all subsequent
     * calls on the context will fail or be no-ops.
     *
     *
     * The typical use case for this function is that the underlying 3D context was lost and further
     * API calls may crash.
     *
     *
     * For Vulkan, even if the device becomes lost, the VkQueue, VkDevice, or VkInstance used to
     * create the context must be kept alive even after abandoning the context. Those objects must
     * live for the lifetime of the context object itself. The reason for this is so that
     * we can continue to delete any outstanding GrBackendTextures/RenderTargets which must be
     * cleaned up even in a device lost state.
     */
    fun abandon() {
        try {
            Stats.onNativeCall()
            _nAbandon(_ptr, 0)
        } finally {
            reachabilityBarrier(this)
        }
    }
}

fun <R> DirectContext.useContext(block: (ctx: DirectContext) -> R): R = use {
    block(this).also { abandon() }
}

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nFlush")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_DirectContext__1nFlush")
private external fun DirectContext_nFlush(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeGL")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_DirectContext__1nMakeGL")
private external fun _nMakeGL(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeMetal")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_DirectContext__1nMakeMetal")
private external fun _nMakeMetal(devicePtr: NativePointer, queuePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeDirect3D")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_DirectContext__1nMakeDirect3D")
private external fun _nMakeDirect3D(adapterPtr: NativePointer, devicePtr: NativePointer, queuePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nSubmit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_DirectContext__1nSubmit")
private external fun _nSubmit(ptr: NativePointer, syncCpu: Boolean)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nReset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_DirectContext__1nReset")
private external fun _nReset(ptr: NativePointer, flags: Int)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nAbandon")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_DirectContext__1nAbandon")
private external fun _nAbandon(ptr: NativePointer, flags: Int)
