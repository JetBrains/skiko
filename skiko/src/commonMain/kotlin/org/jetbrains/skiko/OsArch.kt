package org.jetbrains.skiko

enum class OS(val id: String) {
    Android("android"),
    Linux("linux"),
    Windows("windows"),
    MacOS("macos"),
    Ios("ios"),

    @Deprecated("JS is invalid host OS name. Consider using enum KotlinBackend to detect JS.")
    JS("js"),
    Unknown("unknown")
    ;

    val isLinux
        get() = this == Linux

    val isWindows
        get() = this == Windows

    val isMacOS
        get() = this == MacOS
}

enum class Arch(val id: String) {
    X64("x64"),
    Arm64("arm64"),
    @Deprecated("JS is not valid Arch value")
    JS("js"),
    @Deprecated("WASM is not valid Arch value")
    WASM("wasm"),
    Unknown("unknown"),
    ;
}

enum class KotlinBackend(val id: String) {
    JVM("jvm"),
    JS("js"),
    Native("native"),
    WASM("wasm"),
    ;

    fun isNotJs() = this != JS
}

expect val hostOs: OS

expect val hostArch: Arch

expect val hostId: String

expect val kotlinBackend: KotlinBackend
