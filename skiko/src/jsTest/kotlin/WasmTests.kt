package org.jetbrains.skiko.wasm

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.coroutines.*
import kotlin.js.Promise

external interface ModuleInterface
external val Module: ModuleInterface

suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

internal fun wasmTest(block: () -> Unit) = GlobalScope.promise {
    wasmSetup.await()
    block.invoke()
}

class WasmTests
