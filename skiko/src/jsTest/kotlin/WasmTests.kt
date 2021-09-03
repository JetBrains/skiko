package org.jetbrains.skiko.wasm

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.coroutines.*
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

external interface ModuleInterface {
    fun ping(): Boolean
}

external val ModulePromised: Promise<ModuleInterface>

suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

private fun async(block: suspend () -> Unit): dynamic = GlobalScope.promise {
    block()
}

private fun withModule(block: ModuleInterface.() -> Unit) = async {
    val module = ModulePromised.await()
    block.invoke(module)
}

class WasmTests {
    @Test   
    fun pingTest() = withModule {
        assertTrue(this.ping())
    }
}