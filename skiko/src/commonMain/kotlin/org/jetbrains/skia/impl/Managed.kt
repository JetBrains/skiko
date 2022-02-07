package org.jetbrains.skia.impl

/**
 * Peer for a native object.
 */
expect abstract class Managed(ptr: NativePointer, finalizer: NativePointer, managed: Boolean = true) : Native {
    /**
     * Free underlying native resource, peer is useless afterwards.
     */
    open fun close()

    /**
     * Check if underlying resource is closed.
     */
    open val isClosed: Boolean
}

/**
 * Utility function to use managed object and them immediately release it.
 */
inline fun <T: Managed, R> T.use(block: (T) -> R): R {
    return try {
        block(this)
    } finally {
        this.close()
    }
}