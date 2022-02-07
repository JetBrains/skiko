package org.jetbrains.skia.impl

/**
 * Peer for reference counted native object.
 */
expect abstract class RefCnt : Managed {
    protected constructor(ptr: NativePointer)
    protected constructor(ptr: NativePointer, allowClose: Boolean)

    /**
     * Number of references on underlying native object.
     */
    val refCount: Int
}

expect fun RefCnt_nGetFinalizer(): NativePointer
