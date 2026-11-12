@file:JsModule("./skiko.mjs")
package org.jetbrains.skiko.wasm

import org.jetbrains.skiko.InternalSkikoApi
import kotlin.js.JsAny

@InternalSkikoApi
actual external val awaitSkiko: JsAny
