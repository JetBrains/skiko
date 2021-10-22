package org.jetbrains.skiko

import org.jetbrains.skiko.tests.runTest
import org.khronos.webgl.Int8Array
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ResourcesTest {
    @Test
    fun loadFromPathInlineTest() = runTest {
        val bytes = loadBytesFromPath(resourceURL("./hello_world.txt"))
        assertContentEquals("Hello, world!\n".encodeToByteArray(), bytes)
    }

    @Test
    fun loadFromPathExternTest() = runTest {
        val url = resourceURL("./fonts/FiraCode-Regular.ttf")
        val res = loadBytesFromPath(url).sliceArray(0..9)
        assertContentEquals(byteArrayOf(
            0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x01, 0x00, 0x00, 0x04
        ), res)
    }

    @Test
    fun loadResourceInlineTest() = runTest {
        val bytes = loadResourceAsBytes("./hello_world.txt")
        assertContentEquals("Hello, world!\n".encodeToByteArray(), bytes)
    }

    @Test
    fun loadResourceExternTest() = runTest {
        val res = loadResourceAsBytes("./fonts/FiraCode-Regular.ttf").sliceArray(0..9)
        assertContentEquals(byteArrayOf(
            0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x01, 0x00, 0x00, 0x04
        ), res)
    }

    @Test
    fun loadFromPathByNonLiteralPathTest() = runTest {
        suspend fun loadFont(style: String): ByteArray {
            val url = resourceURL("./fonts/FiraCode-$style.ttf")
            val res = loadBytesFromPath(url).sliceArray(0..9)

            return res
        }

        assertContentEquals(byteArrayOf(
            0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x01, 0x00, 0x00, 0x04
        ), loadFont("Regular"))
    }

    @Test
    fun typedArrayCastTest() {
        val data = Int8Array(byteArrayOf(1, 2, 3, 4, 5).toTypedArray())
        val casted = data.unsafeCast<ByteArray>()
        assertContentEquals(byteArrayOf(1, 2, 3, 4, 5), casted)
    }
}