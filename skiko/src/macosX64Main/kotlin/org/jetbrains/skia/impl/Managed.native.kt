package org.jetbrains.skia.impl

actual abstract class Managed actual constructor(ptr: Long, finalizer: Long, managed: Boolean) : Native(ptr) {
    actual open fun close(): Unit = TODO()
}