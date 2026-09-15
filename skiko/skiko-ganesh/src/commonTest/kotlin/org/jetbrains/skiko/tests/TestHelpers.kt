package org.jetbrains.skiko.tests

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
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
