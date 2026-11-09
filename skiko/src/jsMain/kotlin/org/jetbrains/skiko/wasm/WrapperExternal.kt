@file:JsModule("./js-skiko-reexport-symbols.mjs")
@file:JsNonModule
@file:JsQualifier("api")
package org.jetbrains.skiko.wasm

import org.jetbrains.skiko.InternalSkikoApi
import kotlin.js.JsAny

@InternalSkikoApi
actual external val awaitSkiko: JsAny
