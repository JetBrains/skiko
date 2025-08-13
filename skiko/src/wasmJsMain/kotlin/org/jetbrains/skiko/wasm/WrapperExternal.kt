@file:JsModule("./skiko.mjs")
package org.jetbrains.skiko.wasm

import kotlin.js.Promise

internal external val awaitSkiko: Promise<JsAny?>
