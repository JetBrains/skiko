package org.jetbrains.skia.impl

actual abstract class RefCnt : Managed {
    actual protected constructor(ptr: NativePointer): super(ptr, NullPointer, false) {
        println("TODO: implement native RefCnt")
    }
    actual protected constructor(ptr: NativePointer, allowClose: Boolean): super(ptr, NullPointer, allowClose) {
        println("TODO: implement native RefCnt")
    }
}