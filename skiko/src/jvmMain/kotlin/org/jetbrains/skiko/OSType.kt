package org.jetbrains.skiko

enum class OSType {
    MAC_OS, LINUX, WINDOWS, UNKNOWN;

    companion object {
        private lateinit var currentOSType: OSType
        fun getCurrent(): OSType {
                if (!this::currentOSType.isInitialized) {
                    currentOSType = defineOsType()
                }
                return currentOSType
        }

        private fun defineOsType(): OSType {
            val osName = System.getProperty("os.name").toLowerCase()
            if (isWindows(osName)) {
                return WINDOWS
            }
            if (isMac(osName)) {
                return MAC_OS
            }
            return if (isUnix(osName)) {
                LINUX
            } else UNKNOWN
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