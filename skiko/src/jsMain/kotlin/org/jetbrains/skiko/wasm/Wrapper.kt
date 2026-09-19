package org.jetbrains.skiko.wasm

import kotlin.js.*

/**
 * Invokes a callback [onReady] as soon as onRuntimeInitialized happens.
 * Calling onWasmReady after onRuntimeInitialized invokes [onReady] as well.
 * It's safe to call wasm functions within [onReady] callback, or after it was invoked.
 */
actual fun onWasmReady(onReady: () -> Unit) {
    awaitSkiko.then { onReady() }
}
