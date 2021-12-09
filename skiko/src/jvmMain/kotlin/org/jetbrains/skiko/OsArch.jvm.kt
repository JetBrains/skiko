package org.jetbrains.skiko

actual val hostOs: OS by lazy {
    val osName = System.getProperty("os.name")
    when {
        osName == "Mac OS X" -> OS.MacOS
        osName.startsWith("Win") -> OS.Windows
        "The Android Project" == System.getProperty("java.specification.vendor") -> OS.Android
        osName == "Linux" -> OS.Linux
        else -> throw Error("Unknown OS $osName")
    }
}

actual val hostArch: Arch by lazy {
    val osArch = System.getProperty("os.arch")
    when (osArch) {
        "x86_64", "amd64" -> Arch.X64
        "aarch64" -> Arch.Arm64
        else -> throw Error("Unknown arch $osArch")
    }
}

actual val hostId by lazy {
    "${hostOs.id}-${hostArch.id}"
}

internal val hostFullName by lazy {
    "${System.getProperty("os.name")}, ${System.getProperty("os.version")}, ${hostArch.id}"
}

internal val javaVendor by lazy {
    "${System.getProperty("java.vendor")} ${System.getProperty("java.version")}"
}

internal val javaLocation by lazy {
    "${System.getProperty("java.home")}"
}

actual val kotlinBackend: KotlinBackend
    get() = KotlinBackend.JVM