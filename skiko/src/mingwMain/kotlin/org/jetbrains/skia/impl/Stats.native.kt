package org.jetbrains.skia.impl

import kotlin.native.concurrent.AtomicLong

actual object Stats {
    val enabled = false
    val nativeCalls = AtomicLong(0)
    val allocated = AtomicLong(0)

    actual fun onNativeCall() {
        if (enabled) nativeCalls.increment()
    }

    actual fun onAllocated(className: String) {
        if (enabled) {
            allocated.increment()
            println("AFTER ALLOC: $allocated")
        }
    }

    actual fun onDeallocated(className: String) {
        if (enabled) {
            allocated.decrement()
            println("AFTER DEALLOC: $allocated")
        }
    }
}