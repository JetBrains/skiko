package org.jetbrains.skia.impl

expect abstract class RefCnt : Managed {
    protected constructor(ptr: NativePointer)
    protected constructor(ptr: NativePointer, allowClose: Boolean)

    val refCount: Int

    companion object {
        fun _nGetFinalizer(): NativePointer
    }
}