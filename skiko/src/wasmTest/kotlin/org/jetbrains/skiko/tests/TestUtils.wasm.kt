package org.jetbrains.skiko.tests

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise

actual annotation class SkipJsTarget

actual typealias SkipWasmTarget = kotlin.test.Ignore

@JsFun("() => ''")
private external fun jsRef(): Dynamic

/**
 * Runs the [block] in a coroutine.
 */
actual fun runTest(block: suspend () -> Unit): Any = MainScope().promise {
    block()
    jsRef()
}