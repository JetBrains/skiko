package org.jetbrains.skiko

import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

val hostOSVersion: OSVersion
    get() = NSProcessInfo.processInfo.operatingSystemVersion.useContents {
        OSVersion(
            majorVersion.toInt(),
            minorVersion.toInt(),
            patchVersion.toInt()
        )
    }

fun available(vararg pairs: Pair<OS, OSVersion>) =
    pairs.find { it.first == hostOs }?.let { it.second <= hostOSVersion } ?: false
