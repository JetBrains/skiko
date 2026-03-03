@file:JsModule("./js-reexport-symbols.mjs")
@file:JsNonModule
package org.jetbrains.skiko.wasm

import kotlin.js.Promise

internal actual external val awaitSkiko: Promise<JsAny>
