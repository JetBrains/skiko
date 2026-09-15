package org.jetbrains.skiko.tests

import org.jetbrains.skia.ExternalSymbolName
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
        TestGlContext_nMakeCurrent(nativePtr)
    }

    fun swapBuffers() {
        TestGlContext_nSwapBuffers(nativePtr)
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

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nCreateTestGlContext")
private external fun TestGlContext_nCreate(): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nGlContextGetFinalizer")
private external fun TestGlContext_nGetFinalizer(): NativePointer

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nMakeGlContextCurrent")
private external fun TestGlContext_nMakeCurrent(ptr: NativePointer)

@Suppress("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE") // See KT-44014
@ExternalSymbolName("org_jetbrains_skiko_tests_TestHelpers__1nGlContextSwapBuffers")
private external fun TestGlContext_nSwapBuffers(ptr: NativePointer)