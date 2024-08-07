package org.jetbrains.skia.impl

/**
 * Converts String to zero-terminated utf-8 byte array.
 */
internal fun convertToZeroTerminatedString(string: String): ByteArray {
    //  C++ needs char* with zero byte at the end. So we need to copy array with an extra zero byte.

    val utf8 = string.encodeToByteArray() // encodeToByteArray encodes to utf8
    // TODO Remove array copy, use `skString(data, length)` instead of `skString(data)`
    return utf8.copyOf(utf8.size + 1)
}