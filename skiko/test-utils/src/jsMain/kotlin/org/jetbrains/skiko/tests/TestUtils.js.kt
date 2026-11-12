package org.jetbrains.skiko.tests

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.wasm.awaitSkiko
import kotlin.js.Promise

/**
 * Awaits for skiko wasm (like an application does) and then runs the [block] in a coroutine.
 * Referencing [awaitSkiko] also keeps the `js-skiko-reexport-symbols.mjs` import that publishes the bindings.
 */
@OptIn(InternalSkikoApi::class)
actual fun runTest(block: suspend () -> Unit): TestReturnType = MainScope().promise {
    Promise.resolve(awaitSkiko).await()
    testSetup()
    block()
}

actual typealias SkipJsTarget = kotlin.test.Ignore

actual annotation class SkipWasmTarget
