package org.jetbrains.skiko

import kotlinx.browser.window
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import org.khronos.webgl.Int8Array
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalWasmJsInterop::class)
private val testFetchInit: RequestInit = js("{}")

class ResourcesTest {
    @Test
    fun loadFromPathInlineTest() = runTest {
        val bytes = loadBytesFromPath(resourcePath("./hello_world.txt"))
        assertContentEquals("Hello, world!".encodeToByteArray(), bytes)
    }

    @Test
    fun loadFromPathExternTest() = runTest {
        val url = resourcePath("./fonts/FiraCode-Regular.ttf")
        val res = loadBytesFromPath(url).sliceArray(0..9)
        assertContentEquals(byteArrayOf(
            0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x01, 0x00, 0x00, 0x04
        ), res)
    }

    @Test
    fun loadResourceInlineTest() = runTest {
        val bytes = loadResourceAsBytes("./hello_world.txt")
        assertContentEquals("Hello, world!".encodeToByteArray(), bytes)
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
            val url = resourcePath("./fonts/FiraCode-$style.ttf")
            val res = loadBytesFromPath(url).sliceArray(0..9)

            return res
        }

        assertContentEquals(byteArrayOf(
            0x00, 0x01, 0x00, 0x00, 0x00, 0x12, 0x01, 0x00, 0x00, 0x04
        ), loadFont("Regular"))
    }

    @Test
    fun loadResourceDataFromPathInlineTest() = runTest {
        loadResourceDataFromPath(resourcePath("./hello_world.txt")).use { data ->
            val expectedBytes = "Hello, world!".encodeToByteArray()
            assertEquals(expectedBytes.size, data.size)
            assertContentEquals(expectedBytes, data.bytes)
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    @Test
    fun loadResourceDataFromPathThrowsForMissingResource() = runTest {
        val path = "/missing-resource-for-http-error-test.txt"
        val response = window.fetch(path, testFetchInit).await<Response>()
        assertEquals(404, response.status.toInt())

        val exception = assertFailsWith<IllegalStateException> {
            loadResourceDataFromPath(path).use { }
        }
        assertContains(exception.message.orEmpty(), "Failed to fetch the resource from path=$path")
    }
}
