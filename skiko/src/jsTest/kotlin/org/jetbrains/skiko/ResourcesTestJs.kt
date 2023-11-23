package org.jetbrains.skiko

import org.khronos.webgl.Int8Array
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ResourcesTestJs {
    @Test
    fun typedArrayCastTest() {
        val data = Int8Array(byteArrayOf(1, 2, 3, 4, 5).toTypedArray())
        val casted = data.unsafeCast<ByteArray>()
        assertContentEquals(byteArrayOf(1, 2, 3, 4, 5), casted)
    }
}