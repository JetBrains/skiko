package org.jetbrains.skiko.sample.js

internal class WasmPlatform: Platform {
    override val name: String = "Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()