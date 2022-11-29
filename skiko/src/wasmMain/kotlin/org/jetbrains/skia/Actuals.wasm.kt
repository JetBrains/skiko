package org.jetbrains.skia

actual annotation class ExternalSymbolName(actual val name: String)

actual typealias ExternalCode = kotlin.JsFun

actual typealias InteropType = kotlin.WasmInterop
