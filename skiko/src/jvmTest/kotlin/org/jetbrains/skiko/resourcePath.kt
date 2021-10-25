package org.jetbrains.skiko

import java.nio.file.Paths

private const val RESOURCES_PATH = "src/commonTest/resources"

actual fun resourcePath(resourceId: String) =
    Paths.get("${RESOURCES_PATH}/$resourceId").normalize().toAbsolutePath().toString()
