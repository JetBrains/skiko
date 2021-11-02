package org.jetbrains.skia.impl

import org.jetbrains.skia.Data
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

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun toInteropForArrayOfInteropPointers() = runTest {
        val arrayOfIntArrays = arrayOf(
            intArrayOf(0, 1, 2, 4),
            intArrayOf(100, 200, 300, 400),
            intArrayOf(10000, 20000, 30000, 40000),
        )

        val nativePtr = TestHelpers().writeArrayOfIntArrays(arrayOfIntArrays)

        val memoryOwner = object : Managed(nativePtr, NullPointer, false) {}
        val data = Data.makeWithoutCopy(nativePtr, 3 * 4 * 4, memoryOwner)
        val bytes = data.bytes.toUByteArray().toList()

        val ints: List<Int> = bytes.chunked(4).map {
            (it[3].toInt() shl 24) or (it[2].toInt() shl 16) or (it[1].toInt() shl 8) or (it[0].toInt())
        }

        assertContentEquals(
            expected = arrayOfIntArrays.flatMap { it.toList() }.toTypedArray(),
            actual = ints.toTypedArray()
        )
    }
}
