package org.jetbrains.skia.impl

/**
 * Statistics of native operations.
 */
expect object Stats {
    fun onNativeCall()

    fun onAllocated(className: String)

    fun onDeallocated(className: String)
}