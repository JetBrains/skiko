package org.jetbrains.skia.impl

import runTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals

class InteropScopeTests {

    @Test
    fun canCreateInteropScope() {
        interopScope {  }
    }

    @Test
    fun toInteropString() = runTest {
        interopScope {
            val s = "HelloWorld!ПриветМир!"
            val interopPointer = toInterop(s)

            val readArray = CharArray(s.length)
            interopPointer.fromInterop(readArray)
            assertContentEquals(s.toCharArray(), readArray)
        }
    }

    @Test
    fun toInteropFloatArray() = runTest {
        interopScope {
            val array = floatArrayOf(Float.MIN_VALUE, 2f, -1f, 0f, 1f, Float.MAX_VALUE)
            val interopPointer = toInterop(array)

            val readArray = FloatArray(6)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    fun toInteropByteArray() = runTest {
        interopScope {
            val array = byteArrayOf(Byte.MIN_VALUE, -2, 3, 0, Byte.MAX_VALUE)
            val interopPointer = toInterop(array)

            val readArray = ByteArray(5)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    fun toInteropShortArray() = runTest {
        interopScope {
            val array = shortArrayOf(Short.MIN_VALUE, 2, -3, 0, Short.MAX_VALUE)
            val interopPointer = toInterop(array)

            val readArray = ShortArray(5)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    fun toInteropIntArray() = runTest {
        interopScope {
            val array = intArrayOf(Int.MIN_VALUE, 2, -3, 0, Int.MAX_VALUE)
            val interopPointer = toInterop(array)

            val readArray = IntArray(5)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    @Ignore
    fun toInteropLongArray() = runTest {
        interopScope {
            val array = longArrayOf(Long.MIN_VALUE, -2, 3, 0, Long.MAX_VALUE)
            val interopPointer = toInterop(array)

            val readArray = LongArray(5)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    fun toInteropDoubleArray() = runTest {
        interopScope {
            val array = doubleArrayOf(Double.MIN_VALUE, 2.0, -3.0, 0.0, Double.MAX_VALUE)
            val interopPointer = toInterop(array)

            val readArray = DoubleArray(5)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    fun toInteropStringArray() = runTest {
        interopScope {
            val array = arrayOf("s1", "s2", "s3")
            val interopPointer = toInterop(array)

            val npa = NativePointerArray(3)
            interopPointer.fromInterop(npa)

            val res = generateSequence(0) {
                it + 1
            }.map {
                val charArray = CharArray(2)
                (npa[it] as InteropPointer).fromInterop(charArray)
                charArray.concatToString()
            }.take(3).toList().toTypedArray()

            assertContentEquals(array, res)
        }
    }
}
