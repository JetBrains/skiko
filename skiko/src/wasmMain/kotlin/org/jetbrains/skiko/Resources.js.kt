package org.jetbrains.skiko

import kotlin.js.*
import kotlinx.coroutines.asDeferred

@JsFun("(path) => window.fetch(path).then((r) => r.arrayBuffer()).then((b) => new Int8Array(b))")
private external fun loadResource(path: String): Promise<Dynamic>

@JsFun("(array) => array.length")
private external fun arrayLength(array: Dynamic): Int

@JsFun("(array, index) => array[index]")
private external fun arrayGet(array: Dynamic, index: Int): Byte

actual suspend fun loadBytesFromPath(path: String): ByteArray {
    val resourceArray = loadResource(path).asDeferred<Dynamic>().await()
    return ByteArray(arrayLength(resourceArray)) {
        arrayGet(resourceArray, it)
    }
}

