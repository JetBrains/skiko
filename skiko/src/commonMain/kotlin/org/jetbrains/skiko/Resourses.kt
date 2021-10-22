package org.jetbrains.skiko

expect suspend fun loadBytesFromPath(path: String): ByteArray
