enum class SkiaTarget(
    val id: String,
    val gradleProperties: List<String>
) {
    IOS("ios", listOf("-P${SkikoGradleProperties.AWT_ENABLED}=false")),
    IOS_SIM("iosSim", listOf("-P${SkikoGradleProperties.AWT_ENABLED}=false")),
    MACOS("macos", listOf("-P${SkikoGradleProperties.AWT_ENABLED}=true")),
    WINDOWS("windows", listOf("-P${SkikoGradleProperties.AWT_ENABLED}=true")),
    LINUX("linux", listOf("-P${SkikoGradleProperties.AWT_ENABLED}=true")),
    WASM("wasm", listOf("-P${SkikoGradleProperties.WASM_ENABLED}=true", "-P${SkikoGradleProperties.AWT_ENABLED}=false"));

    private val Arch.gradleProperty: String
        get() = when (this) {
            Arch.X64 -> "x64"
            Arch.Arm64 -> "arm64"
            Arch.Wasm -> "wasm"
        }

    private val Arch.titleCase: String
        get() = when (this) {
            Arch.X64 -> "X64"
            Arch.Arm64 -> "Arm64"
            Arch.Wasm -> "Wasm"
        }

    fun machines(hostArch: Arch): List<Arch> = when (this) {
        IOS -> listOf(if (hostArch == Arch.Arm64) Arch.Arm64 else Arch.X64)
        IOS_SIM -> listOf(if (hostArch == Arch.Arm64) Arch.Arm64 else Arch.X64)
        MACOS -> if (hostArch == Arch.Arm64) listOf(Arch.Arm64, Arch.X64) else listOf(Arch.X64)
        WINDOWS -> listOf(hostArch)
        LINUX -> listOf(hostArch)
        WASM -> listOf(hostArch)
    }

    fun getGradleFlags(hostArch: Arch): List<String> {
        val archProperties = when (this) {
            IOS -> listOf("-P${SkikoGradleProperties.NATIVE_IOS}.${hostArch.gradleProperty}.enabled=true")
            IOS_SIM -> listOf("-P${SkikoGradleProperties.NATIVE_IOS}.simulator${hostArch.titleCase}.enabled=true")
            else -> emptyList()
        }
        return gradleProperties + archProperties
    }

    companion object {
        fun fromString(target: String): SkiaTarget = when (target) {
            "ios" -> IOS
            "iosSim" -> IOS_SIM
            "macos" -> MACOS
            "windows" -> WINDOWS
            "linux" -> LINUX
            "wasm" -> WASM
            else -> throw IllegalArgumentException(
                "Unknown SKIA_TARGET: $target. Valid targets: ios, iosSim, macos, windows, linux, wasm"
            )
        }
    }
}
