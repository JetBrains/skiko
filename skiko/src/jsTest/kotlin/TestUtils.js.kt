package org.jetbrains.skiko.tests

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.jetbrains.skiko.wasm.await
import org.jetbrains.skiko.wasm.wasmSetup

annotation class DoNothing
actual typealias IgnoreTestOnJvm = DoNothing

actual fun runTest(block: suspend () -> Unit): dynamic = GlobalScope.promise {
    wasmSetup.await()
    block()
}
