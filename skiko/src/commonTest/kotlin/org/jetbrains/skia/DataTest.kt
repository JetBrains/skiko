package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DataTest {

    @Test
    fun dataTest() = runTest {
        Data.makeEmpty().use { data ->
            assertEquals(0, data.size)
            assertContentEquals(ByteArray(0), data.bytes)

            Data.makeEmpty().use { data2 -> assertEquals(data, data2) }
        }

        val bytes = "abcdef".toCharArray().map { it.code.toByte() }.toByteArray()
        val bytesSubset = "bcde".toCharArray().map { it.code.toByte() }.toByteArray()

        Data.makeFromBytes(bytes).use { data ->
            assertEquals(6, data.size)
            assertContentEquals(bytes, data.bytes)
            assertContentEquals(bytesSubset, data.getBytes(1, 4))

            Data.makeFromBytes(bytes).use { data2 -> assertEquals(data, data2) }
            data.makeCopy().use { data2 -> assertEquals(data, data2) }

            Data.makeFromBytes(bytes, 1, 4).use { data3 ->
                assertEquals(4, data3.size)
                assertContentEquals(bytesSubset, data3.bytes)
                assertNotEquals(data, data3)
            }

            data.makeSubset(1, 4).use { data4 ->
                assertEquals(4, data4.size)
                assertContentEquals(bytesSubset, data4.bytes)
                assertNotEquals(data, data4)
            }
        }
    }
}
