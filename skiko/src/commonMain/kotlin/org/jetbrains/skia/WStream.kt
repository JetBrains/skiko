package org.jetbrains.skia

import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer

abstract class WStream : Managed {
    constructor(ptr: NativePointer, finalizer: NativePointer) : super(ptr, finalizer)
    constructor(ptr: NativePointer, finalizer: NativePointer, managed: Boolean) : super(ptr, finalizer, managed)
}