package org.jetbrains.skiko.tests

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library
import org.jetbrains.skia.impl.withResult

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

    init {
        Library.staticLoad()
    }
}

@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillByteArrayOf5")
private external fun _nFillByteArrayOf5(interopPointer: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillFloatArrayOf5")
private external fun _nFillFloatArrayOf5(interopPointer: InteropPointer)
