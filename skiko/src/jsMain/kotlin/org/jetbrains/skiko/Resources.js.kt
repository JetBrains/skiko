package org.jetbrains.skiko

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array

actual suspend fun loadBytesFromPath(path: String): ByteArray {
    val arrayBuffer = window.fetch(path)
        .await()
        .arrayBuffer()
        .await()
    val byteArray = Int8Array(arrayBuffer)
    return byteArray.unsafeCast<ByteArray>()
}

