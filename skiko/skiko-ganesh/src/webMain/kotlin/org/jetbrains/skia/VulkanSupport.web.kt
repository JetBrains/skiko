package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skiko.ExperimentalSkikoApi

@OptIn(ExperimentalSkikoApi::class)
internal actual fun nMakeVulkanImpl(
    instancePtr: NativePointer,
    physicalDevicePtr: NativePointer,
    devicePtr: NativePointer,
    queuePtr: NativePointer,
    graphicsQueueIndex: Int,
    instanceProcAddr: NativePointer,
    deviceProcAddr: NativePointer,
    apiVersion: Int,
    memoryAllocator: VulkanMemoryAllocator?
): NativePointer = nMakeVulkanNoAllocator(
    instancePtr, physicalDevicePtr, devicePtr, queuePtr,
    graphicsQueueIndex, instanceProcAddr, deviceProcAddr, apiVersion
)
