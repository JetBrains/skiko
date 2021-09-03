package org.jetbrains.skia.impl

expect abstract class Managed(ptr: Long, finalizer: Long, managed: Boolean = true) : Native {
    open fun close()
    open val isClosed: Boolean
}

inline fun <T: Managed, R> T.use(block: (T) -> R): R {
    return try {
        block(this)
    } finally {
        this.close()
    }
}