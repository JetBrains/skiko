package org.jetbrains.skiko

enum class OSType {
    MAC_OS, LINUX, WINDOWS, UNKNOWN;

    companion object {
        val currentOs by lazy {
            val name = System.getProperty("os.name").toLowerCase()
            when {
                name.indexOf("win") >= 0 -> WINDOWS
                name.indexOf("mac") >= 0 -> MAC_OS
                name.indexOf("linux") >= 0 -> LINUX
                else -> UNKNOWN
            }
        }
    }
}