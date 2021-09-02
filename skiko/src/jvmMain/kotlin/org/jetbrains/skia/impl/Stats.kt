package org.jetbrains.skia.impl

import java.util.concurrent.ConcurrentHashMap

object Stats {
    var enabled = false
    var nativeCalls: Long = 0
    var allocated: MutableMap<String, Int> = ConcurrentHashMap()
    fun onNativeCall() {
        if (enabled) nativeCalls++
    }

    fun onAllocated(className: String) {
        if (enabled) allocated.merge(className, 1) { a: Int?, b: Int? ->
            Integer.sum(
                a!!, b!!
            )
        }
    }

    fun onDeallocated(className: String) {
        if (enabled) allocated.merge(className, -1) { a: Int?, b: Int? ->
            Integer.sum(
                a!!, b!!
            )
        }
    }
}