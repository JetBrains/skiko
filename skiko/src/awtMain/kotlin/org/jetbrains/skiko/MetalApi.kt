package org.jetbrains.skiko

/**
 * Provides [MetalAdapter] that holds pointer to native [MTLDevice](https://developer.apple.com/documentation/metal/mtldevice)
 * chosen using [adapterPriority]
 *
 * @see "src/awtMain/objectiveC/macos/MetalApi.mm"
 */
internal fun chooseMetalAdapter(adapterPriority: GpuPriority): MetalAdapter {
    val adapter = chooseAdapter(adapterPriority.ordinal)
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

private external fun chooseAdapter(adapterPriority: Int): Long
private external fun getAdapterName(adapter: Long): String
private external fun getAdapterMemorySize(adapter: Long): Long