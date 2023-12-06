package org.jetbrains.skiko.tests

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*

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
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nFillByteArrayOf5")
private external fun _nFillByteArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillFloatArrayOf5")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nFillFloatArrayOf5")
private external fun _nFillFloatArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillShortArrayOf5")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nFillShortArrayOf5")
private external fun _nFillShortArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillIntArrayOf5")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nFillIntArrayOf5")
private external fun _nFillIntArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nFillDoubleArrayOf5")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nFillDoubleArrayOf5")
private external fun _nFillDoubleArrayOf5(interopPointer: InteropPointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nWriteArraysOfInts")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nWriteArraysOfInts")
private external fun _nWriteArraysOfInts(interopPointer: InteropPointer): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__nStringByIndex")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__nStringByIndex")
private external fun _nStringByIndex(index: Int): NativePointer

internal fun nativeStringByIndex(index: Int): NativePointer = _nStringByIndex(index)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext")
private external fun TestGlContext_nCreate(): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nGlContextGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nGlContextGetFinalizer")
private external fun TestGlContext_nGetFinalizer(): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent")
private external fun TestGlContext_nMakeCurrent(ptr: NativePointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers")
private external fun TestGlContext_nSwapBuffers(ptr: NativePointer)
