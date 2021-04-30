package org.jetbrains.skiko

enum class OS(val id: String) {
    Linux("linux"),
    Windows("windows"),
    MacOS("macos")
    ;

    val isWindows
        get() = this == Windows
}

enum class Arch(val id: String) {
    X64("x64"),
    Arm64("arm64")
}

val hostOs by lazy {
    val osName = System.getProperty("os.name")
    when {
        osName == "Mac OS X" -> OS.MacOS
        osName == "Linux" -> OS.Linux
        osName.startsWith("Win") -> OS.Windows
        else -> throw Error("Unknown OS $osName")
    }
}

val hostArch by lazy {
    val osArch = System.getProperty("os.arch")
    when (osArch) {
        "x86_64", "amd64" -> Arch.X64
        "aarch64" -> Arch.Arm64
        else -> throw Error("Unknown arch $osArch")
    }
}

val hostId by lazy {
    "${hostOs.id}-${hostArch.id}"
}

internal val hostFullName by lazy {
    "${System.getProperty("os.name")}, ${System.getProperty("os.version")}, ${hostArch.toString().toLowerCase()}"
}

internal val javaVendor by lazy {
    "${System.getProperty("java.vendor")} ${System.getProperty("java.version")}"
}

internal val javaLocation by lazy {
    "${System.getProperty("java.home")}"
}
