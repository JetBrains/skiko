package org.jetbrains.skia.impl

import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.ExternalSymbolName

actual abstract class Native actual constructor(ptr: NativePointer) {
    actual var _ptr: NativePointer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (null == other) return false
        if (other !is Native) return false
        return if (_ptr == other._ptr) true else nativeEquals(other)
    }

    override fun hashCode(): Int = _ptr

    internal actual open fun nativeEquals(other: Native?): Boolean {
        return false
    }

    actual companion object {
        actual val NullPointer: NativePointer
            get() = 0
    }

    actual override fun toString(): String {
        return this::class.simpleName + "(_ptr=0x" + _ptr.toString(16) + ")"
    }

    init {
        if (ptr == NullPointer) throw RuntimeException("Can't wrap nullptr")
        _ptr = ptr
    }
}

internal actual fun reachabilityBarrier(obj: Any?) {}

actual typealias NativePointer = Int
actual typealias InteropPointer = Int

private val INTEROP_SCOPE = InteropScope()
private var interopScopeCounter = 0

internal actual inline fun <T> interopScope(block: InteropScope.() -> T): T {
    try {
        interopScopeCounter++
        return INTEROP_SCOPE.block()
    } finally {
        interopScopeCounter--
        if (interopScopeCounter == 0) {
            INTEROP_SCOPE.release()
        }
    }
}


// Those functions are defined by Emscripten.
@ExternalSymbolName("_malloc")
@ModuleImport("./skiko.mjs", "malloc")
internal external fun _malloc(size: Int): NativePointer

@ExternalSymbolName("_free")
@ModuleImport("./skiko.mjs", "free")
internal external fun _free(ptr: NativePointer)

private external fun lengthBytesUTF8(str: String): Int

internal expect fun stringToUTF8(str: String, outPtr: NativePointer, maxBytesToWrite: Int)

private external fun UTF8ToString(ptr: NativePointer): String

// Data copying routines.
internal expect fun toWasm(dest: NativePointer, src: ByteArray)
internal expect fun toWasm(dest: NativePointer, src: ShortArray)
internal expect fun toWasm(dest: NativePointer, src: CharArray)
internal expect fun toWasm(dest: NativePointer, src: FloatArray)
internal expect fun toWasm(dest: NativePointer, src: DoubleArray)
internal expect fun toWasm(dest: NativePointer, src: IntArray)

internal expect fun fromWasm(src: NativePointer, result: ByteArray)
internal expect fun fromWasm(src: NativePointer, result: ShortArray)
internal expect fun fromWasm(src: NativePointer, result: IntArray)
internal expect fun fromWasm(src: NativePointer, result: FloatArray)
internal expect fun fromWasm(src: NativePointer, result: DoubleArray)

actual class NativePointerArray actual constructor(size: Int) {
    internal val backing = IntArray(size)
    actual operator fun get(index: Int): NativePointer {
        return backing[index]
    }

    actual operator fun set(index: Int, value: NativePointer) {
        backing[index] = value
    }

    actual val size: Int
        get() = backing.size

    companion object {
        internal fun fromIntArray(intArray: IntArray): NativePointerArray {
            return NativePointerArray(intArray.size).apply {
                intArray.copyInto(backing)
            }
        }
    }
}
