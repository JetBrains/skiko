package org.jetbrains.skiko

import org.khronos.webgl.Int8Array

internal actual fun Int8Array.asByteArray(): ByteArray = this.unsafeCast<ByteArray>()
