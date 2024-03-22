package org.jetbrains.skiko

import kotlin.js.*
import kotlinx.coroutines.asDeferred

@JsFun("(path) => window.fetch(path).then((r) => r.arrayBuffer()).then((b) => new Int8Array(b))")
private external fun loadResource(path: String): Promise<JsAny>

@JsFun("(array) => array.length")
private external fun arrayLength(array: JsAny): Int

@JsFun("(array, index) => array[index]")
private external fun arrayGet(array: JsAny, index: Int): Byte

actual suspend fun loadBytesFromPath(path: String): ByteArray {
    val resourceArray = loadResource(path).asDeferred<JsAny>().await()
    return ByteArray(arrayLength(resourceArray)) {
        arrayGet(resourceArray, it)
    }
}

