package org.jetbrains.skiko.tests

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.kotlinBackend

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

        fun isAvailable(): Boolean {
            if (hostOs != OS.Linux || kotlinBackend != KotlinBackend.Native) {
                // TODO implement for other platforms and render targets
                return false
            }
            return true
        }

        inline fun <T> run(block: TestGlContext.() -> T): T {
           check(isAvailable()) { "TestGlContext is not available" }
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

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext")
// Needs ForceNativeThreadState: opens an X display, creates a GLX graphics context, and waits for the X server to
// report that the window is mapped.
// Native call path: XOpenDisplay -> glXCreateNewContext -> XMapWindow -> XIfEvent
@ForceNativeThreadState
private external fun TestGlContext_nCreate(): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nGlContextGetFinalizer")
// No ForceNativeThreadState: only returns the address of a delete function, so it is short and cannot block.
private external fun TestGlContext_nGetFinalizer(): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent")
// Needs ForceNativeThreadState: binds a GL context, which makes a round trip to the X server and a call into the GL
// driver.
// Native call path: glXMakeContextCurrent
@ForceNativeThreadState
private external fun TestGlContext_nMakeCurrent(ptr: NativePointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers")
// Needs ForceNativeThreadState: presents the GL surface, which can wait for vsync or the X server.
// Native call path: glXSwapBuffers
@ForceNativeThreadState
private external fun TestGlContext_nSwapBuffers(ptr: NativePointer)
