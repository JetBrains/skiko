package org.jetbrains.skiko

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.js

private fun jsThrow(e: JsAny) {
    js("throw e;")
}

@JsFun("function (f) { try { f(); } catch (e) { return e;}; return null; }")
private external fun jsCatch(f: () -> Unit): JsAny?

/**
 * For a Dynamic value caught in JS, returns the corresponding [Throwable]
 * if it was thrown from Kotlin, or null otherwise.
 */
@ExperimentalWasmJsInterop
private fun JsAny.toThrowableOrNull(): Throwable? {
    val thisAny: Any = this
    if (thisAny is Throwable) return thisAny
    var result: Throwable? = null
    jsCatch {
        try {
            jsThrow(this)
        } catch (e: Throwable) {
            result = e
        }
    }
    return result
}

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalWasmJsInterop::class)
internal suspend fun <T> Promise<JsAny?>.await(): T = suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
    this@await.then(
        onFulfilled = { cont.resume(it as T); null },
        onRejected = { cont.resumeWithException(it.toThrowableOrNull() ?: error("Unexpected non-Kotlin exception $it")); null }
    )
}