package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.ExperimentalSkikoApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

@OptIn(ExperimentalSkikoApi::class)
internal actual fun withTestGraphiteContext(block: (GraphiteContext) -> Unit) {
    when (hostOs) {
        OS.MacOS -> makeTestMetalContext().use(block)
        OS.Linux, OS.Windows -> withTestVulkanContext(block)
        else -> Unit
    }
}

@OptIn(ExperimentalSkikoApi::class)
private fun makeTestMetalContext(): GraphiteContext {
    GraphiteLibrary.load()
    val metalObjects = _nCreateMetalObjects()
    check(metalObjects.size == 2) { "Failed to create test Metal objects" }
    val context = try {
        GraphiteContext.makeMetal(metalObjects[0], metalObjects[1])
    } finally {
        _nReleaseMetalObjects(metalObjects[0], metalObjects[1])
    }
    return context
}

@OptIn(ExperimentalSkikoApi::class)
private fun withTestVulkanContext(block: (GraphiteContext) -> Unit) {
    GraphiteLibrary.load()
    val vulkanObjects = _nCreateVulkanObjects()
    if (vulkanObjects.isEmpty()) return
    check(vulkanObjects.size == 6) { "Failed to create test Vulkan objects" }
    val context = try {
        GraphiteContext.makeVulkan(
            instancePtr = vulkanObjects[0],
            physicalDevicePtr = vulkanObjects[1],
            devicePtr = vulkanObjects[2],
            queuePtr = vulkanObjects[3],
            graphicsQueueIndex = vulkanObjects[4].toInt(),
            maxApiVersion = vulkanObjects[5].toInt(),
        )
    } catch (throwable: Throwable) {
        _nReleaseVulkanObjects(vulkanObjects[2], vulkanObjects[0])
        throw throwable
    }
    try {
        block(context)
    } finally {
        // Graphite borrows the Vulkan instance and device, so destroy them after the context.
        context.close()
        _nReleaseVulkanObjects(vulkanObjects[2], vulkanObjects[0])
    }
}

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteTestHelpers__1nCreateMetalObjects")
private external fun _nCreateMetalObjects(): LongArray

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteTestHelpers__1nReleaseMetalObjects")
private external fun _nReleaseMetalObjects(devicePtr: NativePointer, queuePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteTestHelpers__1nCreateVulkanObjects")
private external fun _nCreateVulkanObjects(): LongArray

@ExternalSymbolName("org_jetbrains_skia_gpu_graphite_GraphiteTestHelpers__1nReleaseVulkanObjects")
private external fun _nReleaseVulkanObjects(devicePtr: NativePointer, instancePtr: NativePointer)
