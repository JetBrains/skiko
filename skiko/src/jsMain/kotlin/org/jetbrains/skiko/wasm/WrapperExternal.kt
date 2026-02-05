@file:JsModule("./js-reexport-symbols.mjs")
@file:JsNonModule
@file:JsQualifier("api")
package org.jetbrains.skiko.wasm

import kotlin.js.Promise

internal external val awaitSkiko: Promise<Any>
