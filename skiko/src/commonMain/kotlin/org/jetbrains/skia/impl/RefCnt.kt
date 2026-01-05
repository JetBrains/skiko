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

internal fun RefCnt.refCntToString(managedString: String, nullPointer: NativePointer) = buildString {
    append(managedString)
    setLength(length - 1)  // remove trailing ')'

    if (_ptr == nullPointer) {
        append(", disposed")
    } else {
        append(", refCount=")
        append(refCount)
    }

    append(')')
}

internal expect fun RefCnt_nGetFinalizer(): NativePointer
