package org.jetbrains.skia.impl

expect class NativePointerArray constructor(size: Int) {
    operator fun get(index: Int): NativePointer
    operator fun set(index: Int, value: NativePointer)
    val size: Int
}


