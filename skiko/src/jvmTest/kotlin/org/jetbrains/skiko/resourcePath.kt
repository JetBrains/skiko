package org.jetbrains.skiko

private const val RESOURCES_PATH = "src/commonTest/resources"

actual fun resourcePath(resourceId: String) = "${RESOURCES_PATH}/$resourceId"
