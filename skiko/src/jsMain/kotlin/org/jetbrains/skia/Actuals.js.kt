package org.jetbrains.skia

actual typealias ExternalSymbolName = kotlin.js.JsName

actual annotation class ExternalCode(actual val code: String)

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
actual annotation class InteropType()