package org.jetbrains.skia

actual typealias ExternalSymbolName = kotlin.js.JsName

@Target(AnnotationTarget.FUNCTION)
actual annotation class ModuleImport(
    actual val module: String,
    actual val name: String
)


actual typealias QuasiJsModule = kotlin.js.JsModule
actual typealias QuasiJsNonModule = kotlin.js.JsNonModule
actual typealias QuasiJsQualifier = kotlin.js.JsQualifier
