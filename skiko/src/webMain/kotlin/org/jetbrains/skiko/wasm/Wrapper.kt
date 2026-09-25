package org.jetbrains.skiko.wasm

import org.jetbrains.skiko.InternalSkikoApi
import kotlin.js.JsAny
import kotlin.js.Promise

internal expect fun onWasmReady(onReady: () -> Unit)

@InternalSkikoApi
expect val awaitSkiko: Promise<JsAny>