package org.jetbrains.skia.impl

import runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class InteropScopeCommonTests {

    @Test
    fun withResultByteArray() = runTest {
        val byteArray = ByteArray(5)
        TestHelpers()._nFillByteArrayOf5(byteArray)

        assertContentEquals(byteArrayOf(1, 2, 3, 4, 5), byteArray)
    }

    @Test
    fun withResultFloatArray() = runTest {
        val floatArray = FloatArray(5)
        TestHelpers()._nFillFloatArrayOf5(floatArray)

        assertContentEquals(floatArrayOf(0.0f, 1.1f, 2.2f, 3.3f, -4.4f), floatArray)
    }
}
