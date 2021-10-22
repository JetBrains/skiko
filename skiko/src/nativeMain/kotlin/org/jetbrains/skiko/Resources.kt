package org.jetbrains.skiko

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.*

actual suspend fun loadBytesFromPath(path: String): ByteArray {
    val file = fopen(path, "r") ?: throw Error("Can not open file '$path'")
    val size = file.let {
        fseek(it, 0, SEEK_END)
        val size = ftell(it)
        fseek(it, 0, SEEK_SET)
        size
    }

    if (size < 0) {
        fclose(file)
        throw Error("Can not read file '$path'")
    }

    if (size > Int.MAX_VALUE.toLong()) {
        fclose(file)
        throw Error("File '$path' is too long")
    }

    if (size == 0L) {
        fclose(file)
        return byteArrayOf()
    }

    val bytes = ByteArray(size.toInt())
    val result = bytes.usePinned {
        fread(it.addressOf(0), 1, size.toULong(), file)
    }
    fclose(file)

    if (result != size.toULong()) {
        throw Error("Can not read file '$path'")
    }

    return bytes
}
