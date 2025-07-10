package org.jetbrains.skia

//actual annotation class ExternalSymbolName(actual val name: String)
actual typealias ExternalSymbolName = kotlin.js.JsName

@Target(AnnotationTarget.FUNCTION)
actual annotation class ModuleImport(
    actual val module: String,
    actual val name: String
)