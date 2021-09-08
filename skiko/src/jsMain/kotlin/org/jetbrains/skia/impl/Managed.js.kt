package org.jetbrains.skia.impl

actual abstract class Managed actual constructor(ptr: NativePointer, finalizer: NativePointer, managed: Boolean) : Native(ptr) {
    actual open fun close(): Unit = TODO()

    actual open val isClosed: Boolean
        get() = _ptr == NULLPNTR
}