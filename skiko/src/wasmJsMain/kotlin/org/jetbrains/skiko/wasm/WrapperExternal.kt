@file:JsModule("./skiko.mjs")
package org.jetbrains.skiko.wasm

import org.jetbrains.skiko.InternalSkikoApi
import kotlin.js.Promise

@InternalSkikoApi
actual external val awaitSkiko: Promise<JsAny>
