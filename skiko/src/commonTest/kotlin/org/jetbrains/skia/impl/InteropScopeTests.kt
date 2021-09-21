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
            val array = floatArrayOf(-1f, 2f, 3f, 0f, 5f)
            val interopPointer = toInterop(array)

            val readArray = FloatArray(5)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    fun toInteropByteArray() = runTest {
        interopScope {
            val array = byteArrayOf(-1, 2, 3, 0, 5)
            val interopPointer = toInterop(array)

            val readArray = ByteArray(5)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    fun toInteropShortArray() = runTest {
        interopScope {
            val array = shortArrayOf(-1, 2, 3, 0, 5)
            val interopPointer = toInterop(array)

            val readArray = ShortArray(5)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    fun toInteropIntArray() = runTest {
        interopScope {
            val array = intArrayOf(-1, 2, 3, 0, 5)
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
            val array = longArrayOf(-1, 2, 3, 0, 5)
            val interopPointer = toInterop(array)

            val readArray = LongArray(5)
            interopPointer.fromInterop(readArray)
            assertContentEquals(array, readArray)
        }
    }

    @Test
    fun toInteropDoubleArray() = runTest {
        interopScope {
            val array = doubleArrayOf(-1.0, 2.0, 3.0, 0.0, 5.0)
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

//            interopPointer.fromInterop(readArray)
//            assertContentEquals(array, readArray)
        }
    }

//    @Test
//    fun toInteropNativePointerArray() = runTest {
//        interopScope {
//            val array = createTestNativePointerArray()
//            val interopPointer = toInterop(array)
//
//            val readArray = NativePointerArray(array.size)
//            //interopPointer.fromInterop(readArray)
//        }
//    }
}
