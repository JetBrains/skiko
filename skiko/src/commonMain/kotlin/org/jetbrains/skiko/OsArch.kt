package org.jetbrains.skiko

enum class OS(val id: String) {
    Android("android"),
    Linux("linux"),
    Windows("windows"),
    MacOS("macos"),
    Ios("ios"),
    Tvos("tvos"),
    JS("js")
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
    JS("js"),
    WASM("wasm")
}

enum class KotlinBackend(val id: String) {
    JVM("jvm"),
    JS("js"),
    Native("native"),
    ;

    fun isNotJs() = this != JS
}

expect val hostOs: OS

expect val hostArch: Arch

expect val hostId: String

expect val kotlinBackend: KotlinBackend
