package org.jetbrains.skiko

actual val hostOs: OS by lazy {
    when (Platform.osFamily) {
        OsFamily.MACOSX -> OS.MacOS
        OsFamily.LINUX -> OS.Linux
        OsFamily.WINDOWS -> OS.Windows
        OsFamily.IOS -> OS.Ios
        OsFamily.TVOS -> OS.Tvos
        else -> throw Error("Unsupported OS ${Platform.osFamily}")
    }
}

actual val hostArch: Arch by lazy {
    when (Platform.cpuArchitecture) {
        CpuArchitecture.X64 -> Arch.X64
        CpuArchitecture.ARM64 -> Arch.Arm64
        else -> throw Error("Unsupported arch ${Platform.cpuArchitecture}")
    }
}

actual val hostId by lazy {
    "${hostOs.id}-${hostArch.id}"
}

actual val kotlinBackend: KotlinBackend
    get() = KotlinBackend.Native