package org.jetbrains.skiko

import javax.accessibility.Accessible

/**
 * Provides [MetalAdapter] that holds pointer to native [MTLDevice](https://developer.apple.com/documentation/metal/mtldevice)
 * chosen using [adapterPriority]
 *
 * @see "src/awtMain/objectiveC/macos/MetalApi.mm"
 */
internal fun chooseMetalAdapter(adapterPriority: GpuPriority): MetalAdapter {
    val adapter = chooseAdapter(adapterPriority.ordinal)

    if (adapter == 0L) {
        throw RenderException("MetalApi.chooseAdapter returned null")
    }

    val adapterName = getAdapterName(adapter)
    val adapterMemorySize = getAdapterMemorySize(adapter)

    return MetalAdapter(adapter, adapterName, adapterMemorySize)
}

/**
 * @param ptr pointer for native [MTLDevice](https://developer.apple.com/documentation/metal/mtldevice)
 * @param name the full name of the vendor device.
 * @param memorySize approximation of how much memory this device can use with good performance.
 */
internal data class MetalAdapter(val ptr: Long, val name: String, val memorySize: Long)

internal fun MetalAdapter.dispose() {
    disposeAdapter(ptr)
}

/**
 * [@autoreleasepool](https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/MemoryMgmt/Articles/mmAutoreleasePools.html)
 */
@Suppress("SpellCheckingInspection")
internal inline fun <R> autoreleasepool(block: () -> R): R {
    val handle = openAutoreleasepool()
    return try {
        block()
    } finally {
        closeAutoreleasepool(handle)
    }
}

private external fun chooseAdapter(adapterPriority: Int): Long
private external fun disposeAdapter(adapter: Long)
private external fun getAdapterName(adapter: Long): String
private external fun getAdapterMemorySize(adapter: Long): Long

@Suppress("SpellCheckingInspection")
private external fun openAutoreleasepool(): Long

@Suppress("SpellCheckingInspection")
private external fun closeAutoreleasepool(handle: Long)

internal external fun initializeCAccessible(accessible: Accessible)