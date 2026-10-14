package org.jetbrains.skiko

actual val hostOs: OS = OS.Unknown

actual val hostArch: Arch = Arch.Unknown

actual val hostId: String = "wasm"

actual val kotlinBackend: KotlinBackend
    get() = KotlinBackend.Wasm
