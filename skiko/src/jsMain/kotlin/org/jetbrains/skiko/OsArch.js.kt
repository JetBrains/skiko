package org.jetbrains.skiko

actual val hostOs: OS = OS.JS

actual val hostArch: Arch = Arch.JS

actual val hostId by lazy {
    "${hostOs.id}-${hostArch.id}"
}

actual val kotlinBackend: KotlinBackend
    get() = KotlinBackend.JS