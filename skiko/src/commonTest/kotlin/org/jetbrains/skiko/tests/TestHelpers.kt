package org.jetbrains.skiko.tests

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*

class TestHelpers {

    fun _nFillByteArrayOf5(array: ByteArray) {
        withResult(array) {
            _nFillByteArrayOf5(it)
        }
    }

    fun _nFillFloatArrayOf5(array: FloatArray) {
        withResult(array) {
            _nFillFloatArrayOf5(it)
        }
    }

    fun _nFillShortArrayOf5(array: ShortArray) {
        withResult(array) {
            _nFillShortArrayOf5(it)
        }
    }

    fun _nFillIntArrayOf5(array: IntArray) {
        withResult(array) {
            _nFillIntArrayOf5(it)
        }
    }

    fun _nFillDoubleArrayOf5(array: DoubleArray) {
        withResult(array) {
            _nFillDoubleArrayOf5(it)
        }
    }

    fun writeArrayOfIntArrays(array: Array<IntArray>): NativePointer {
        require(array.size == 3) {
            "For testing purposes, the length of the array should be 3"
        }
        return interopScope {
            _nWriteArraysOfInts(
                toInteropForArraysOfPointers(
                    array.map { toInterop(it) }.toTypedArray()
                )
            )
        }
    }

    init {
        Library.staticLoad()
    }
}

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillByteArrayOf5")
// No ForceNativeThreadState: writes five constant elements into the caller's array, so it is short and cannot block.
private external fun _nFillByteArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillFloatArrayOf5")
// No ForceNativeThreadState: writes five constant elements into the caller's array, so it is short and cannot block.
private external fun _nFillFloatArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillShortArrayOf5")
// No ForceNativeThreadState: writes five constant elements into the caller's array, so it is short and cannot block.
private external fun _nFillShortArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillIntArrayOf5")
// No ForceNativeThreadState: writes five constant elements into the caller's array, so it is short and cannot block.
private external fun _nFillIntArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillDoubleArrayOf5")
// No ForceNativeThreadState: writes five constant elements into the caller's array, so it is short and cannot block.
private external fun _nFillDoubleArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nWriteArraysOfInts")
// No ForceNativeThreadState: copies a fixed 12 ints into a small allocated block, so it is short and cannot block.
// Native call path: malloc
private external fun _nWriteArraysOfInts(interopPointer: InteropPointer): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__nStringByIndex")
// Needs ForceNativeThreadState: its default case reaches a TODO path that prints and flushes stdout, which can block on
// I/O.
// Native call path: TODO -> printf -> fflush
@ForceNativeThreadState
private external fun _nStringByIndex(index: Int): NativePointer

internal fun nativeStringByIndex(index: Int): NativePointer = _nStringByIndex(index)
