package org.jetbrains.skiko

import org.jetbrains.skia.Data
import org.junit.Assert.assertArrayEquals
import java.nio.charset.StandardCharsets
import kotlin.test.Test

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DataTest {
    @Test
    fun dataTest() {
        Data.makeEmpty().use { data ->
            assertEquals(0L, data.size)
            assertArrayEquals(ByteArray(0), data.bytes)

            Data.makeEmpty().use { data2 -> assertEquals(data, data2) }
        }

        val bytes = "abcdef".toByteArray(StandardCharsets.UTF_8)
        val bytesSubset = "bcde".toByteArray(StandardCharsets.UTF_8)

        Data.makeFromBytes(bytes).use { data ->
            assertEquals(6L, data.size)
            assertArrayEquals(bytes, data.bytes)
            assertArrayEquals(bytesSubset, data.getBytes(1, 4))

            Data.makeFromBytes(bytes).use { data2 -> assertEquals(data, data2) }
            data.makeCopy().use { data2 -> assertEquals(data, data2) }

            Data.makeFromBytes(bytes, 1, 4).use { data3 ->
                assertEquals(4L, data3.size)
                assertArrayEquals(bytesSubset, data3.bytes)
                assertNotEquals(data, data3)
            }

            data.makeSubset(1, 4).use { data4 ->
                assertEquals(4L, data4.size)
                assertArrayEquals(bytesSubset, data4.bytes)
                assertNotEquals(data, data4)
            }
        }
    }
}