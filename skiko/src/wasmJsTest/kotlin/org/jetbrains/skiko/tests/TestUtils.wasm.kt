package org.jetbrains.skiko.tests

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise

actual annotation class SkipJsTarget

actual typealias SkipWasmTarget = kotlin.test.Ignore

@JsFun("() => ''")
private external fun jsRef(): JsAny

/**
 * Runs the [block] in a coroutine.
 */
actual fun <T> runTest(block: suspend () -> Unit): T {
    error("It's a fake actual. Not expected to be called")
}

fun runTest(block: suspend () -> Unit): Any = MainScope().promise {
    block()
    jsRef()
}
