package org.jetbrains.skiko

import org.jetbrains.skia.Data
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.webext.copyBufferToSkiko
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.Promise
import kotlin.js.js

internal expect fun Int8Array.asByteArray(): ByteArray

// Without request init passed explicitly, js target fails
// this looks like a bug in kotlinx.browser definitions
// TODO: to discuss with the Kotlin team
private val defaultFetchInit: RequestInit = js("{}")

private fun fetchResource(path: String, init: RequestInit): Promise<Response> =
    js("globalThis.fetch(path, init)")

@OptIn(ExperimentalWasmJsInterop::class)
actual suspend fun loadBytesFromPath(path: String): ByteArray {
    val arrayBuffer = fetchResource(path, defaultFetchInit)
        .await<Response>()
        .arrayBuffer()
        .await<ArrayBuffer>()
    return Int8Array(arrayBuffer).asByteArray()
}

@OptIn(ExperimentalWasmJsInterop::class)
@InternalSkikoApi
/**
 * Fetches the resource as ArrayBuffer from the [path] and copies it to skiko memory.
 * @return Data containing the resources' bytes.
 * Skia takes ownership of the memory and frees it when the last native reference is released.
 */
suspend fun loadResourceDataFromPath(path: String): Data {
    val response = fetchResource(path, defaultFetchInit).await<Response>()
    if (!response.ok) {
        throw IllegalStateException("Failed to fetch the resource from path=$path - ${response.statusText}")
    }
    val arrayBuffer = response.arrayBuffer().await<ArrayBuffer>()
    val ptr = copyBufferToSkiko(arrayBuffer)
    if (ptr == Native.NullPointer) {
        throw IllegalStateException("Failed to allocate memory for resource '$path'")
    }
    return Data.makeFromMalloc(ptr, arrayBuffer.byteLength)
}
