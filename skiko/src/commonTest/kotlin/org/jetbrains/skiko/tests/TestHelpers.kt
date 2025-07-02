package org.jetbrains.skiko.tests

import org.jetbrains.skia.impl.Library
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.impl.withResult


internal class TestGlContext : Managed(TestGlContext_nCreate(), FinalizerHolder.PTR) {
    private object FinalizerHolder {
        val PTR = TestGlContext_nGetFinalizer()
    }

    fun makeCurrent() {
        TestGlContext_nMakeCurrent(_ptr)
    }

    fun swapBuffers() {
        TestGlContext_nSwapBuffers(_ptr)
    }

    companion object {
        inline fun <T> run(block: TestGlContext.() -> T): T {
           return TestGlContext().use {
                it.makeCurrent()
                val result = it.block()
                it.swapBuffers()
               result
            }
        }
    }
}

class TestHelpers {

    fun _nFillByteArrayOf5(array: ByteArray) {
        withResult(array) {
            TestGlContext_nFillByteArrayOf5(it)
        }
    }

    fun _nFillFloatArrayOf5(array: FloatArray) {
        withResult(array) {
            TestGlContext_nFillFloatArrayOf5(it)
        }
    }

    fun _nFillShortArrayOf5(array: ShortArray) {
        withResult(array) {
            TestGlContext_nFillShortArrayOf5(it)
        }
    }

    fun _nFillIntArrayOf5(array: IntArray) {
        withResult(array) {
            TestGlContext_nFillIntArrayOf5(it)
        }
    }

    fun _nFillDoubleArrayOf5(array: DoubleArray) {
        withResult(array) {
            TestGlContext_nFillDoubleArrayOf5(it)
        }
    }

    fun writeArrayOfIntArrays(array: Array<IntArray>): NativePointer {
        require(array.size == 3) {
            "For testing purposes, the length of the array should be 3"
        }
        return interopScope {
            TestGlContext_nWriteArraysOfInts(
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

internal fun nativeStringByIndex(index: Int): NativePointer = TestGlContext_nStringByIndex(index)