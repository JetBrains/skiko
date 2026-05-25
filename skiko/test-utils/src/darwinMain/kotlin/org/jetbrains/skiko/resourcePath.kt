package org.jetbrains.skiko

import platform.Foundation.NSBundle
import platform.Foundation.NSURL

private val KEXE_DIR: String = NSBundle.mainBundle.bundlePath
private const val RESOURCES_PATH = "src/commonTest/resources"

actual fun resourcePath(resourceId: String) = run {
    val filePath = "$KEXE_DIR/../../../../$RESOURCES_PATH/$resourceId"
    // Remove all '..' and '.'
    val standardized = NSURL.URLWithString(filePath)?.standardizedURL?.absoluteString
    standardized ?: filePath
}
