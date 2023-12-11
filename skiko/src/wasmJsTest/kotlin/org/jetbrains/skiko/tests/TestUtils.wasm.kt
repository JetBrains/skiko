package org.jetbrains.skiko.tests

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise

actual annotation class SkipJsTarget

actual typealias SkipWasmTarget = kotlin.test.Ignore

@JsFun("() => ''")
private external fun jsRef(): JsAny


actual typealias TestReturnType = Any
/**
 * Runs the [block] in a coroutine.
 */
actual fun runTest(block: suspend () -> Unit): TestReturnType = MainScope().promise {
    block()
    jsRef()
}
