package org.jetbrains.skiko

expect fun resourcePath(resourceId: String): String

suspend inline fun loadResourceAsBytes(resourcePath: String): ByteArray
    = loadBytesFromPath(resourcePath(resourcePath))