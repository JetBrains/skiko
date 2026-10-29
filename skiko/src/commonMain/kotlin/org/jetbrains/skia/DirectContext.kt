package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.loadOpenGLLibrary
import kotlin.jvm.JvmStatic

class DirectContext internal constructor(ptr: NativePointer, managed: Boolean = true) : RefCnt(ptr, managed) {
    companion object {
        @JvmStatic
        fun makeGL(): DirectContext = nativeCallFromStatic {
            loadOpenGLLibrary()
            val ptr = _nMakeGL()
            if (ptr == NullPointer) throw RenderException("Can't create OpenGL DirectContext")
            DirectContext(ptr)
        }

        @JvmStatic
        fun makeMetal(devicePtr: NativePointer, queuePtr: NativePointer): DirectContext = nativeCallFromStatic {
            DirectContext(_nMakeMetal(devicePtr, queuePtr))
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
        @JvmStatic
        fun makeDirect3D(adapterPtr: NativePointer, devicePtr: NativePointer, queuePtr: NativePointer): DirectContext = nativeCallFromStatic {
            DirectContext(_nMakeDirect3D(adapterPtr, devicePtr, queuePtr))
        }

        /**
         * Creates Vulkan direct rendering context from Vulkan native objects.
         *
         * @param instancePtr pointer to VkInstance; must not be null
         * @param physicalDevicePtr pointer to VkPhysicalDevice; must not be null
         * @param devicePtr pointer to VkDevice; must not be null
         * @param queuePtr pointer to VkQueue; must not be null
         * @param graphicsQueueIndex index of the graphics queue used for rendering
         * @param instanceProcAddr pointer to vkGetInstanceProcAddr; must not be null
         * @param deviceProcAddr pointer to vkGetDeviceProcAddr; must not be null
         * @param apiVersion Vulkan API version
         * @param memoryAllocator custom allocator for Vulkan device memory, or null to use
         *                        Skia's built-in allocator
         */
        @ExperimentalSkikoApi
        @JvmStatic
        fun makeVulkan(
            instancePtr: NativePointer,
            physicalDevicePtr: NativePointer,
            devicePtr: NativePointer,
            queuePtr: NativePointer,
            graphicsQueueIndex: Int,
            instanceProcAddr: NativePointer,
            deviceProcAddr: NativePointer,
            apiVersion: Int,
            memoryAllocator: VulkanMemoryAllocator? = null
        ): DirectContext = nativeCallFromStatic {
            require(instancePtr != NullPointer) { "instancePtr must not be null" }
            require(physicalDevicePtr != NullPointer) { "physicalDevicePtr must not be null" }
            require(devicePtr != NullPointer) { "devicePtr must not be null" }
            require(queuePtr != NullPointer) { "queuePtr must not be null" }
            require(graphicsQueueIndex >= 0) { "graphicsQueueIndex must be non-negative" }
            require(instanceProcAddr != NullPointer) { "instanceProcAddr must not be null" }
            require(deviceProcAddr != NullPointer) { "deviceProcAddr must not be null" }

            val ptr = nMakeVulkanImpl(
                instancePtr,
                physicalDevicePtr,
                devicePtr,
                queuePtr,
                graphicsQueueIndex,
                instanceProcAddr,
                deviceProcAddr,
                apiVersion,
                memoryAllocator
            )
            if (ptr == NullPointer) throw RenderException("Can't create Vulkan DirectContext")
             DirectContext(ptr)
        }

        init {
            staticLoad()
        }
    }

    fun flush(): DirectContext = nativeCall {
        DirectContext_nFlushDefault(_ptr)
        this
    }

    fun flush(surface: Surface): DirectContext = nativeCall(surface) { surface ->
        DirectContext_nFlush(_ptr, surface._ptr)
        this
    }

    fun resetAll(): DirectContext = nativeCall {
        _nReset(_ptr, -1)
        this
    }

    fun resetGLAll(): DirectContext = nativeCall {
        _nReset(_ptr, 0xffff)
        this
    }

    fun resetGL(vararg states: GLBackendState): DirectContext = nativeCall {
        var flags = 0
        for (state in states) flags = flags or state._bit
        _nReset(_ptr, flags)
        this
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
    fun submit(syncCpu: Boolean) = nativeCall { _nSubmit(_ptr, syncCpu) }

    fun flushAndSubmit(surface: Surface, syncCpu: Boolean = false) =
        nativeCall(surface) { surface -> _nFlushAndSubmit(_ptr, surface._ptr, syncCpu) }

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
    fun abandon() = nativeCall { _nAbandon(_ptr, 0) }

    /**
     * GPU resource cache limit. If the cache currently exceeds this limit,
     * it will be purged (LRU) to keep the cache within the limit.
     */
    var resourceCacheLimit: Long
        get() = nativeCall { DirectContext_nGetResourceCacheLimit(_ptr) }
        set(value) = nativeCall { DirectContext_nSetResourceCacheLimit(_ptr, value) }

}

fun <R> DirectContext.useContext(block: (ctx: DirectContext) -> R): R = use {
    block(this).also { abandon() }
}

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nFlush")
// Needs ForceNativeThreadState: sends queued drawing work to the GPU through Metal.
// Native call path: GrDirectContext::flush -> GrDrawingManager::flushSurfaces -> GrMtlGpu::onSubmitToGpu
@ForceNativeThreadState
private external fun DirectContext_nFlush(ptr: NativePointer, surfacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nFlushDefault")
// Needs ForceNativeThreadState: sends queued drawing work to the GPU through Metal.
// Native call path: GrDirectContext::flush -> GrDrawingManager::flushSurfaces -> GrMtlGpu::onSubmitToGpu
@ForceNativeThreadState
private external fun DirectContext_nFlushDefault(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nGetResourceCacheLimit")
// Needs ForceNativeThreadState: in debug builds, can wait for another thread that is using the same GPU context.
// Native call path: GrDirectContext::getResourceCacheLimit -> SingleOwner::enter -> SkMutex::acquire ->
//   SkSemaphore::wait
@ForceNativeThreadState
private external fun DirectContext_nGetResourceCacheLimit(ptr: NativePointer): Long

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nSetResourceCacheLimit")
// Needs ForceNativeThreadState: can free cached GPU resources, such as Metal textures, to fit the new limit.
// Native call path: GrDirectContext::setResourceCacheLimit -> GrResourceCache::setLimit -> purgeAsNeeded
@ForceNativeThreadState
private external fun DirectContext_nSetResourceCacheLimit(ptr: NativePointer, maxResourceBytes: Long)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeGL")
// Needs ForceNativeThreadState: creates a GPU context and queries OpenGL capabilities, which makes GL calls.
// Native call path: GrDirectContexts::MakeGL -> GrGLGpu::Make
@ForceNativeThreadState
private external fun _nMakeGL(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeMetal")
// Needs ForceNativeThreadState: creates a GPU context and queries the Metal device, which calls Metal.
// Native call path: GrDirectContexts::MakeMetal -> GrMtlTrampoline::MakeGpu -> GrMtlGpu::Make
@ForceNativeThreadState
private external fun _nMakeMetal(devicePtr: NativePointer, queuePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeDirect3D")
// Needs ForceNativeThreadState: creates a Direct3D 12 GPU context, which is GPU setup work.
// Native call path: GrDirectContexts::MakeD3D -> GrD3DGpu::Make
@ForceNativeThreadState
private external fun _nMakeDirect3D(adapterPtr: NativePointer, devicePtr: NativePointer, queuePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeVulkan")
// Needs ForceNativeThreadState: creates a Vulkan GPU context, which is GPU setup work.
// Native call path: GrDirectContexts::MakeVulkan -> GrVkGpu::Make
@ForceNativeThreadState
private external fun _nMakeVulkan(
    instancePtr: NativePointer,
    physicalDevicePtr: NativePointer,
    devicePtr: NativePointer,
    queuePtr: NativePointer,
    graphicsQueueIndex: Int,
    instanceProcAddr: NativePointer,
    deviceProcAddr: NativePointer,
    apiVersion: Int,
    memoryAllocatorPtr: NativePointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nSubmit")
// Needs ForceNativeThreadState: commits the Metal command buffer to the GPU, and can wait for the GPU to finish.
// Native call path: GrDirectContext::submit -> GrGpu::submitToGpu -> GrMtlGpu::onSubmitToGpu ->
//   GrMtlCommandBuffer::commit
@ForceNativeThreadState
private external fun _nSubmit(ptr: NativePointer, syncCpu: Boolean)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nFlushAndSubmit")
// Needs ForceNativeThreadState: flushes queued drawing work and commits it through Metal, and can wait for the GPU to
// finish.
// Native call path: GrDirectContext::flushAndSubmit -> GrMtlGpu::onSubmitToGpu -> GrMtlCommandBuffer::commit
@ForceNativeThreadState
private external fun _nFlushAndSubmit(ptr: NativePointer, surfacePtr: NativePointer, syncCpu: Boolean)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nReset")
// Needs ForceNativeThreadState: in debug builds, can wait for another thread that is using the same GPU context before
// resetting it.
// Native call path: GrDirectContext::resetContext -> SingleOwner::enter -> SkMutex::acquire -> SkSemaphore::wait
@ForceNativeThreadState
private external fun _nReset(ptr: NativePointer, flags: Int)

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nAbandon")
// Needs ForceNativeThreadState: waits for all outstanding GPU work, then releases the Metal queue and encoders.
// Native call path: GrDirectContext::abandonContext -> GrMtlGpu::disconnect
@ForceNativeThreadState
private external fun _nAbandon(ptr: NativePointer, flags: Int)

internal fun nMakeVulkanNoAllocator(
    instancePtr: NativePointer,
    physicalDevicePtr: NativePointer,
    devicePtr: NativePointer,
    queuePtr: NativePointer,
    graphicsQueueIndex: Int,
    instanceProcAddr: NativePointer,
    deviceProcAddr: NativePointer,
    apiVersion: Int
): NativePointer = _nMakeVulkan(
    instancePtr, physicalDevicePtr, devicePtr, queuePtr,
    graphicsQueueIndex, instanceProcAddr, deviceProcAddr, apiVersion,
    NullPointer
)
