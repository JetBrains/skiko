package org.jetbrains.skiko

private const val RESOURCES_PATH = "src/commonTest/resources"

actual fun resourcePath(resourceId: String): String = "$RESOURCES_PATH/$resourceId"
