package org.jetbrains.skiko

import platform.Foundation.NSURL

private const val RESOURCES_PATH = "src/commonTest/resources"

internal expect fun basePath(): String

actual fun resourcePath(resourceId: String) = run {
    val filePath = "${basePath()}/$RESOURCES_PATH/$resourceId"
    // Remove all '..' and '.'
    val standardized = NSURL.URLWithString(filePath)?.standardizedURL?.absoluteString
    standardized ?: filePath
}
