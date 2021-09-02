package org.jetbrains.skia.impl

import java.util.concurrent.ConcurrentHashMap

actual object Stats {
    var enabled = false
    var nativeCalls: Long = 0
    var allocated: MutableMap<String, Int> = ConcurrentHashMap()
    actual fun onNativeCall() {
        if (enabled) nativeCalls++
    }

    actual fun onAllocated(className: String) {
        if (enabled) allocated.merge(className, 1) { a: Int, b: Int ->
            a + b
        }
    }

    actual fun onDeallocated(className: String) {
        if (enabled) allocated.merge(className, -1) { a: Int, b: Int ->
            a + b
        }
    }
}