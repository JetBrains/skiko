package org.jetbrains.skia.impl

expect object Stats {
    fun onNativeCall()

    fun onAllocated(className: String)

    fun onDeallocated(className: String)
}