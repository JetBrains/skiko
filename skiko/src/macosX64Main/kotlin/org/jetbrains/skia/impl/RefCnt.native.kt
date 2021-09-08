package org.jetbrains.skia.impl

actual abstract class RefCnt : Managed {
    actual protected constructor(ptr: NativePointer): super(ptr, 0L, false) {
        println("TODO: implement native RefCnt")
    }
    actual protected constructor(ptr: NativePointer, allowClose: Boolean): super(ptr, 0, allowClose) {
        println("TODO: implement native RefCnt")
    }
}