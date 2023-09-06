package org.jetbrains.skia.impl

import kotlin.concurrent.AtomicLong

actual object Stats {
    val enabled = false
    val nativeCalls = AtomicLong(0)
    val allocated = AtomicLong(0)

    actual fun onNativeCall() {
        if (enabled) nativeCalls.incrementAndGet()
    }

    actual fun onAllocated(className: String) {
        if (enabled) {
            allocated.incrementAndGet()
            println("AFTER ALLOC: $allocated")
        }
    }

    actual fun onDeallocated(className: String) {
        if (enabled) {
            allocated.decrementAndGet()
            println("AFTER DEALLOC: $allocated")
        }
    }
}