package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.NativePointerArray
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skiko.ExperimentalSkikoApi

/**
 * The main entry point for Graphite, responsible for managing and coordinating GPU resources.
 */
@ExperimentalSkikoApi
class GraphiteContext internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            GraphiteLibrary.load()
        }

        /**
         * Creates a Graphite context that submits work to a Metal command queue.
         *
         * @param devicePtr native pointer to the Metal device.
         * @param queuePtr native pointer to the Metal command queue.
         * @return a Graphite context backed by Metal.
         */
        fun makeMetal(devicePtr: NativePointer, queuePtr: NativePointer): GraphiteContext {
            requireMetalSupport()
            require(devicePtr != NullPointer) { "Metal device pointer is null" }
            require(queuePtr != NullPointer) { "Metal queue pointer is null" }
            Stats.onNativeCall()
            val ptr = _nMakeMetal(devicePtr, queuePtr)
            check(ptr != NullPointer) { "Failed to create a Graphite Metal context" }
            return GraphiteContext(ptr)
        }

        /**
         * Creates a Graphite context that submits work to a Vulkan queue.
         *
         * The Vulkan objects are owned by the caller and must outlive the returned context.
         *
         * @param instancePtr native pointer to the `VkInstance`.
         * @param physicalDevicePtr native pointer to the `VkPhysicalDevice`.
         * @param devicePtr native pointer to the `VkDevice`.
         * @param queuePtr native pointer to the `VkQueue` the work is submitted to.
         * @param graphicsQueueIndex index of the queue family [queuePtr] belongs to; it must
         * support graphics operations.
         * @param maxApiVersion highest Vulkan API version (`VK_MAKE_API_VERSION`) Skia may use.
         * @return a Graphite context backed by Vulkan.
         */
        fun makeVulkan(
            instancePtr: NativePointer,
            physicalDevicePtr: NativePointer,
            devicePtr: NativePointer,
            queuePtr: NativePointer,
            graphicsQueueIndex: Int,
            maxApiVersion: Int,
        ): GraphiteContext {
            requireVulkanSupport()
            require(instancePtr != NullPointer) { "Vulkan instance pointer is null" }
            require(physicalDevicePtr != NullPointer) { "Vulkan physical device pointer is null" }
            require(devicePtr != NullPointer) { "Vulkan device pointer is null" }
            require(queuePtr != NullPointer) { "Vulkan queue pointer is null" }
            Stats.onNativeCall()
            val ptr = _nMakeVulkan(
                instancePtr,
                physicalDevicePtr,
                devicePtr,
                queuePtr,
                graphicsQueueIndex,
                maxApiVersion,
            )
            check(ptr != NullPointer) { "Failed to create a Graphite Vulkan context" }
            return GraphiteContext(ptr)
        }
    }

    /**
     * Creates a [Recorder] that records drawing commands for this context.
     *
     * @return a new recorder.
     */
    fun makeRecorder(): Recorder {
        return try {
            Stats.onNativeCall()
            val ptr = _nMakeRecorder(nativePtr)
            check(ptr != NullPointer) { "Failed to create a Graphite recorder" }
            Recorder(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Adds a [recording] to this context's pending GPU work.
     *
     * The work is sent to the GPU by a subsequent call to [submit].
     *
     * @param recording recording to insert.
     */
    fun insertRecording(recording: Recording) {
        insertRecording(InsertRecordingInfo(recording))
    }

    /**
     * Adds a recording and associated submission metadata (such as wait/signal semaphores)
     * to this context's pending GPU work.
     *
     * @param info recording insertion parameters.
     */
    fun insertRecording(info: InsertRecordingInfo) {
        try {
            Stats.onNativeCall()
            val waitSemPtrs = NativePointerArray(info.waitSemaphores.size)
            val signalSemPtrs = NativePointerArray(info.signalSemaphores.size)
            for (index in info.waitSemaphores.indices) {
                waitSemPtrs[index] = info.waitSemaphores[index].nativePtr
            }
            for (index in info.signalSemaphores.indices) {
                signalSemPtrs[index] = info.signalSemaphores[index].nativePtr
            }
            interopScope {
                _nInsertRecording(
                    nativePtr,
                    info.recording.nativePtr,
                    toInterop(waitSemPtrs),
                    info.waitSemaphores.size,
                    toInterop(signalSemPtrs),
                    info.signalSemaphores.size,
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(info.recording)
            for (sem in info.waitSemaphores) {
                reachabilityBarrier(sem)
            }
            for (sem in info.signalSemaphores) {
                reachabilityBarrier(sem)
            }
        }
    }

    /**
     * Submits pending work to the GPU.
     *
     * @param syncCpu if `true`, waits for the submitted GPU work to finish before returning.
     */
    fun submit(syncCpu: Boolean = false) {
        try {
            Stats.onNativeCall()
            _nSubmit(nativePtr, syncCpu)
        } finally {
            reachabilityBarrier(this)
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetGraphiteContextFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nGetFinalizer")
private external fun _nGetGraphiteContextFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nMakeMetal")
private external fun _nMakeMetal(devicePtr: NativePointer, queuePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nMakeVulkan")
private external fun _nMakeVulkan(
    instancePtr: NativePointer,
    physicalDevicePtr: NativePointer,
    devicePtr: NativePointer,
    queuePtr: NativePointer,
    graphicsQueueIndex: Int,
    maxApiVersion: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nMakeRecorder")
private external fun _nMakeRecorder(contextPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nInsertRecording")
private external fun _nInsertRecording(
    contextPtr: NativePointer,
    recordingPtr: NativePointer,
    waitSemaphoresPtrs: InteropPointer,
    waitSemaphoresCount: Int,
    signalSemaphoresPtrs: InteropPointer,
    signalSemaphoresCount: Int,
)

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteContext__1nSubmit")
private external fun _nSubmit(contextPtr: NativePointer, syncCpu: Boolean)
