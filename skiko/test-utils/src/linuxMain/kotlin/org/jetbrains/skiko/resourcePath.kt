package org.jetbrains.skiko

import platform.posix.realpath
import platform.posix.PATH_MAX
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned

private const val RESOURCES_PATH = "src/commonTest/resources"

actual fun resourcePath(resourceId: String) = run {
    val filePath = "$RESOURCES_PATH/$resourceId"
    // Remove all '..' and '.'
    var buffer = ByteArray(PATH_MAX)
    val standardized = buffer.usePinned {
        realpath(filePath, it.addressOf(0))?.toKString()
    }
    standardized ?: filePath
}
