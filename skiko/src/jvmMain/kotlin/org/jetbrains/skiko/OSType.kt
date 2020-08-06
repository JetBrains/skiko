package org.jetbrains.skiko

enum class OSType {
    MAC_OS, LINUX, WINDOWS, UNKNOWN;

    companion object {
        val currentOs = defineOsType()

        private fun defineOsType(): OSType {
            val osName = System.getProperty("os.name").toLowerCase()
            return when {
                isWindows(osName) -> WINDOWS
                isMac(osName) -> MAC_OS
                isUnix(osName) -> LINUX
                else -> UNKNOWN
            }
        }

        private fun isWindows(name: String): Boolean {
            return name.indexOf("win") >= 0
        }

        private fun isMac(name: String): Boolean {
            return name.indexOf("mac") >= 0
        }

        private fun isUnix(name: String): Boolean {
            return name.indexOf("nix") >= 0 || name.indexOf("nux") >= 0 || name.indexOf("aix") >= 0
        }
    }
}