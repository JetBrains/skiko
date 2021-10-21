package org.jetbrains.skiko

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.jetbrains.skia.ExternalSymbolName
import org.khronos.webgl.Int8Array
import kotlin.js.Promise

@ExternalSymbolName("require")
external fun resourceURL(resource: String): String

suspend fun loadBytesFromPath(path: String): ByteArray {
    val arrayBuffer = window.fetch(path)
        .await()
        .arrayBuffer()
        .await()
    val byteArray = Int8Array(arrayBuffer)
    return byteArray.unsafeCast<ByteArray>()
}

suspend inline fun loadResourceAsBytes(resourcePath: String): ByteArray {
    return loadBytesFromPath(resourceURL(resourcePath))
}

//actual suspend inline fun resourceBytes(resourcePath: String): ByteArray = loadResourceAsBytes(resourcePath)