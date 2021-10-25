package org.jetbrains.skiko.tests

import org.jetbrains.skia.ExternalSymbolName
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

    init {
        Library.staticLoad()
    }
}

@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillByteArrayOf5")
private external fun _nFillByteArrayOf5(interopPointer: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillFloatArrayOf5")
private external fun _nFillFloatArrayOf5(interopPointer: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillShortArrayOf5")
private external fun _nFillShortArrayOf5(interopPointer: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillIntArrayOf5")
private external fun _nFillIntArrayOf5(interopPointer: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillDoubleArrayOf5")
private external fun _nFillDoubleArrayOf5(interopPointer: InteropPointer)
