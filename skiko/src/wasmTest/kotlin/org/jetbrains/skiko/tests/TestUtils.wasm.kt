package org.jetbrains.skiko.tests

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual annotation class SkipJsTarget

actual typealias SkipWasmTarget = kotlin.test.Ignore

/**
 * Awaits for `wasmSetup` and then runs the [block] in a coroutine.
 */
actual fun runTest(block: suspend () -> Unit) {
    GlobalScope.launch {
        block()
    }
}