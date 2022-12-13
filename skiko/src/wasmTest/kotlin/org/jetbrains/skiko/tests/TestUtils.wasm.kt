package org.jetbrains.skiko.tests

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise

actual annotation class SkipJsTarget

actual typealias SkipWasmTarget = kotlin.test.Ignore

/**
 * Awaits for `wasmSetup` and then runs the [block] in a coroutine.
 */

actual fun runTest(block: suspend () -> Unit): Any = MainScope().promise {
    block()
}