@file:JsModule("./skiko.mjs")
@file:JsNonModule
@file:JsQualifier("skikoApi")
package org.jetbrains.skia.impl

internal external fun _registerCallback(cb: () -> Unit, data: Any?, global: Boolean): Int
internal external fun _createLocalCallbackScope()
internal external fun _releaseLocalCallbackScope()