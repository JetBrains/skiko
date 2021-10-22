package org.jetbrains.skiko

import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ResourceTest {
    @Test
    fun loadFromPathTest() = runTest {
        val bytes = loadBytesFromPath(resourcePath("./hello_world.txt"))
        assertContentEquals("Hello, world!\n".encodeToByteArray(), bytes)
    }

    @Test
    fun loadResourceTest() = runTest {
        val bytes = loadResourceAsBytes("./hello_world.txt")
        assertContentEquals("Hello, world!\n".encodeToByteArray(), bytes)
    }

    @Test
    fun loadFontTest() = runTest {
        val url = resourcePath("./fonts/FiraCode-Regular.ttf")
        val res = loadBytesFromPath(url).sliceArray(0..9)
        assertContentEquals(byteArrayOf(
            0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x01, 0x00, 0x00, 0x04
        ), res)
    }
}