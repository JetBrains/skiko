package org.jetbrains.skia.impl

actual object Stats {
    actual fun onNativeCall() {}

    actual fun onAllocated(className: String) {}

    actual fun onDeallocated(className: String) {}
}