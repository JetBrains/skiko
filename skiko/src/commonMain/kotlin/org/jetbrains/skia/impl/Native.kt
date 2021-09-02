package org.jetbrains.skia.impl

expect abstract class Native(ptr: Long) {
    var _ptr: Long

    companion object {
        fun getPtr(n: Native?): Long
    }
}

expect fun reachabilityBarrier(obj: Any?)