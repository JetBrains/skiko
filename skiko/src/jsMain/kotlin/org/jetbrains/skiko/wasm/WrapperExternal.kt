@file:JsModule("./js-reexport-symbols.mjs")
@file:JsNonModule
@file:JsQualifier("api")
package org.jetbrains.skiko.wasm

import kotlin.js.Promise

external val awaitSkiko: Promise<Unit>
