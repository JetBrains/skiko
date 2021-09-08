package org.jetbrains.skia.impl

actual abstract class RefCnt : Managed {
    actual protected constructor(ptr: NativePointer): super(ptr, NullPointer, false) { TODO() }
    actual protected constructor(ptr: NativePointer, allowClose: Boolean): super(ptr, 0, allowClose) { TODO() }
}