package org.jetbrains.skiko.tests

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise

/**
 * Awaits for `wasmSetup` and then runs the [block] in a coroutine.
 */
actual fun runTest(block: suspend () -> Unit): TestReturnType = MainScope().promise {
    testSetup()
    block()
}

actual typealias SkipJsTarget = kotlin.test.Ignore

actual annotation class SkipWasmTarget
