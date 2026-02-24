package org.jetbrains.skiko

import org.khronos.webgl.Int8Array
import org.khronos.webgl.get

internal actual fun Int8Array.asByteArray(): ByteArray = ByteArray(length) { index -> this[index] }