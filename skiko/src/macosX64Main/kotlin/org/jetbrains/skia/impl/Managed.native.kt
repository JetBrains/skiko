package org.jetbrains.skia.impl

actual abstract class Managed actual constructor(ptr: Long, finalizer: Long, managed: Boolean) : Native(ptr) {
    actual open fun close(): Unit = TODO()
    actual open val isClosed: Boolean
        get() = _ptr == 0L
}