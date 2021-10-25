package org.jetbrains.skiko

import java.io.File

actual suspend fun loadBytesFromPath(path: String): ByteArray {
    return File(path).readBytes()
}
