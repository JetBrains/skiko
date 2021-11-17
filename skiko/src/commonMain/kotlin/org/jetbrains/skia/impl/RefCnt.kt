package org.jetbrains.skia.impl

expect abstract class RefCnt : Managed {
    protected constructor(ptr: NativePointer)
    protected constructor(ptr: NativePointer, allowClose: Boolean)

    val refCount: Int
}

expect fun RefCnt_nGetFinalizer(): NativePointer
