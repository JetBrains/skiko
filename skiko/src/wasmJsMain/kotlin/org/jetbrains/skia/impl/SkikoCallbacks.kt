@file:JsModule("./skiko.mjs")
@file:JsQualifier("skikoApi")
package org.jetbrains.skia.impl

// See `setup.mjs`
internal external fun _registerCallback(cb: () -> Unit, data: JsAny?, global: Boolean): Int
internal external fun _createLocalCallbackScope()
internal external fun _releaseLocalCallbackScope()