package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skiko.ExperimentalSkikoApi
import kotlin.jvm.JvmStatic

/**
 * Represents a backend-specific semaphore that Graphite can use for synchronization.
 */
@ExperimentalSkikoApi
class BackendSemaphore internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            GraphiteLibrary.load()
        }

        /**
         * Creates a Graphite backend semaphore wrapping a Vulkan `VkSemaphore`.
         *
         * The returned [BackendSemaphore] does not take ownership of [semaphorePtr] and will not
         * delete the Vulkan semaphore when it is closed.
         *
         * @param semaphorePtr native pointer (`VkSemaphore`) to wrap.
         * @return a backend semaphore wrapping the Vulkan semaphore.
         */
        @JvmStatic
        fun makeVulkan(semaphorePtr: NativePointer): BackendSemaphore {
            requireVulkanSupport()
            require(semaphorePtr != NullPointer) { "Vulkan semaphore pointer is null" }
            Stats.onNativeCall()
            val ptr = _nMakeVulkan(semaphorePtr)
            check(ptr != NullPointer) { "Failed to create a Graphite Vulkan backend semaphore" }
            return BackendSemaphore(ptr)
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetBackendSemaphoreFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_BackendSemaphore__1nGetFinalizer")
// No ForceNativeThreadState: returns the function that frees the object, without calling it, so it is short and cannot
// block.
// Native call path: deleteBackendSemaphore
private external fun _nGetBackendSemaphoreFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_BackendSemaphore__1nMakeVulkan")
// No ForceNativeThreadState: returns 0 without touching its argument, so it is short and cannot block.
private external fun _nMakeVulkan(semaphorePtr: NativePointer): NativePointer
