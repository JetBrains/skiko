package org.jetbrains.skia

actual typealias ExternalSymbolName = kotlin.js.JsName

@Target(AnnotationTarget.FUNCTION)
actual annotation class ModuleImport(
    actual val name: String
)