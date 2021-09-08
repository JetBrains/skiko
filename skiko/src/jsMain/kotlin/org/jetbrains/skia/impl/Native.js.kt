package org.jetbrains.skia.impl

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    actual open fun _nativeEquals(other: Native?): Boolean = TODO()

    actual companion object {
        actual val NULLPNTR: NativePointer
            get() = 0
    }

    init {
        if (ptr == NULLPNTR) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

actual fun reachabilityBarrier(obj: Any?) {
    TODO()
}

actual typealias NativePointer = Int

actual fun Int.toNativePointer(): NativePointer = this