package org.jetbrains.skia.impl

import org.jetbrains.skiko.tests.TestHelpers
import org.jetbrains.skiko.tests.runTest
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

    @Test
    fun withResultShortArray() = runTest {
        val shortArray = ShortArray(5)
        TestHelpers()._nFillShortArrayOf5(shortArray)

        assertContentEquals(shortArrayOf(0, 1, 2, -3, 4), shortArray)
    }

    @Test
    fun withResultIntArray() = runTest {
        val intArray = IntArray(5)
        TestHelpers()._nFillIntArrayOf5(intArray)

        assertContentEquals(intArrayOf(0, 1, -22, 3, 4), intArray)
    }

    @Test
    fun withResultDoubleArray() = runTest {
        val doubleArray = DoubleArray(5)
        TestHelpers()._nFillDoubleArrayOf5(doubleArray)

        assertContentEquals(doubleArrayOf(-0.001, 0.00222, 2.71828, 3.1415, 10000000.9991), doubleArray)
    }
}
