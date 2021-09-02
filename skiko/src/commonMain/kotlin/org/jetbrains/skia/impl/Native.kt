package org.jetbrains.skia.impl

expect abstract class Native(ptr: Long) {
    var _ptr: Long

    companion object {
        fun getPtr(n: Native?): Long
    }

    open fun _nativeEquals(other: Native?): Boolean
}

expect fun reachabilityBarrier(obj: Any?)
