package org.jetbrains.skiko.tests

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import org.jetbrains.skiko.wasm.awaitSkiko

actual annotation class SkipJsTarget

actual typealias SkipWasmTarget = kotlin.test.Ignore

@JsFun("() => ''")
private external fun jsRef(): JsAny
/**
 * Runs the [block] in a coroutine.
 */
actual fun runTest(block: suspend () -> Unit): TestReturnType = MainScope().promise {
    awaitSkiko.await<Any>()
    block()
    jsRef()
}
