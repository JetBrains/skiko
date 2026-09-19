@file:JsModule("./js-skiko-reexport-symbols.mjs")
@file:JsNonModule
@file:JsQualifier("api")
package org.jetbrains.skiko.wasm

import org.jetbrains.skiko.InternalSkikoApi
import kotlin.js.Promise

@InternalSkikoApi
actual external val awaitSkiko: Promise<JsAny>
