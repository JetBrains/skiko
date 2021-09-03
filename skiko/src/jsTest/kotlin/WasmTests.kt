package org.jetbrains.skiko.wasm

//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.promise
import kotlin.coroutines.*
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

external interface ModuleInterface {
    fun <T> cwrap(vararg args: Any): T
}

fun ModuleInterface.fib(n: Int): Int {
    return cwrap<(Int) -> Int>("fib", "number", arrayOf("number"))(n)
}

external val ModulePromised: Promise<ModuleInterface>

suspend fun <T> Promise<T>.await(): T = suspendCoroutine { cont ->
    then({ cont.resume(it) }, { cont.resumeWithException(it) })
}

//private fun async(block: suspend () -> Unit): dynamic = GlobalScope.promise {
//    block()
//}
//
//private fun withModule(block: ModuleInterface.() -> Unit) = async {
//    val module = ModulePromised.await()
//    block.invoke(module)
//}

class WasmTests {
//    @Test
//    fun moduleTest() = withModule {
//        assertEquals(2, fib(12))
//    }
}