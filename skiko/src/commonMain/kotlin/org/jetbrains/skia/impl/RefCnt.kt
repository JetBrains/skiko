package org.jetbrains.skia.impl

expect abstract class RefCnt : Managed {
    protected constructor(ptr: NativePointer)
    protected constructor(ptr: NativePointer, allowClose: Boolean)
}