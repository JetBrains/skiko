package org.jetbrains.skiko

internal fun chooseMetalAdapter(adapterPriority: GpuPriority): MetalAdapter {
    val adapter = chooseAdapter(adapterPriority.ordinal)
    val adapterName = getAdapterName(adapter)
    val adapterMemorySize = getAdapterMemorySize(adapter)

    return MetalAdapter(adapter, adapterName, adapterMemorySize)
}

internal data class MetalAdapter(val ptr: Long, val name: String, val memorySize: Long)

private external fun chooseAdapter(adapterPriority: Int): Long
private external fun getAdapterName(adapter: Long): String
private external fun getAdapterMemorySize(adapter: Long): Long