package org.jetbrains.skija.impl

import org.jetbrains.annotations.ApiStatus

enum class Platform {
    WINDOWS, LINUX, MACOS_X64, MACOS_ARM64;

    companion object {
        @ApiStatus.Internal
        val _values = values()
        val CURRENT: Platform? = null

        init {
            val os = System.getProperty("os.name").toLowerCase()
            if (org.jetbrains.skija.impl.os.contains("mac") || org.jetbrains.skija.impl.os.contains("darwin")) {
                if ("aarch64" == System.getProperty("os.arch")) CURRENT = MACOS_ARM64 else CURRENT = MACOS_X64
            } else if (org.jetbrains.skija.impl.os.contains("windows")) CURRENT =
                WINDOWS else if (org.jetbrains.skija.impl.os.contains("nux") || org.jetbrains.skija.impl.os.contains("nix")) CURRENT =
                LINUX else throw RuntimeException("Unsupported platform: " + org.jetbrains.skija.impl.os)
        }
    }
}