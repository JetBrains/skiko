package org.jetbrains.skia.impl

expect abstract class Managed(ptr: Long, finalizer: Long, managed: Boolean = true) : Native {
    open fun close()
}