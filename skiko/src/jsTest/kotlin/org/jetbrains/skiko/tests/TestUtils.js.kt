package org.jetbrains.skiko.tests

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import org.jetbrains.skiko.wasm.wasmSetup
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

actual typealias SkipJsTarget = kotlin.test.Ignore

actual annotation class SkipWasmTarget

private suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

/**
 * Awaits for `wasmSetup` and then runs the [block] in a coroutine.
 */
actual fun runTest(block: suspend () -> Unit): dynamic = MainScope().promise {
    wasmSetup.await()
    block()
}