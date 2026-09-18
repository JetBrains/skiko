@file:JsModule("./skiko-ganesh.mjs")
package org.jetbrains.skiko

@InternalSkikoApi
actual external val GL: GLInterface

internal external actual fun isSideModuleLoaded(): Boolean
