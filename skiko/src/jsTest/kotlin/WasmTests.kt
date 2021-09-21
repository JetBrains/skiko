package org.jetbrains.skiko.wasm

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.coroutines.*
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

external interface ModuleInterface
external val Module: ModuleInterface

suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

private fun wasmTest(block: () -> Unit) = GlobalScope.promise {
    wasmSetup.await()
    block.invoke()
}

class WasmTests {
    @Test
    fun pingTest() = wasmTest {
    }
}