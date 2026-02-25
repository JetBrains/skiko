package org.jetbrains.skiko

import kotlinx.browser.window
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

internal expect fun Int8Array.asByteArray(): ByteArray

// Without request init passed explicitly, js target fails
// this looks like a bug in kotlinx.browser definitions
// TODO: to discuss with the Kotlin team
private val defaultFetchInit: RequestInit = js("{}")

@OptIn(ExperimentalWasmJsInterop::class)
actual suspend fun loadBytesFromPath(path: String): ByteArray {
    val arrayBuffer = window.fetch(path, defaultFetchInit)
        .await<Response>()
        .arrayBuffer()
        .await<ArrayBuffer>()
    return Int8Array(arrayBuffer).asByteArray()
}