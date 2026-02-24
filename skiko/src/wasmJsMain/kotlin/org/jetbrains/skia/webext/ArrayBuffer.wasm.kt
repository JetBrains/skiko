package org.jetbrains.skia.webext

import kotlinx.coroutines.await
import org.jetbrains.skiko.wasm.awaitSkiko

internal actual suspend fun getSkikoWasm(): SkikoWasm {
    return awaitSkiko.await()
}