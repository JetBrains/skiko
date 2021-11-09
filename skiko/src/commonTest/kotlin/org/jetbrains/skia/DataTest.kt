package org.jetbrains.skia

import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.use
import org.jetbrains.skiko.tests.runTest
import kotlin.test.*

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

    @Test
    fun failsToReadMoreBytesThanPossible() = runTest {
        val bytes = byteArrayOf(1, 2, 3, 4, 5)
        val data = Data.makeFromBytes(bytes)


        data.use {
            assertFailsWith<IllegalStateException> {
                it.getBytes(2, 10)
            }

            assertFailsWith<IllegalStateException> {
                it.getBytes(0, 6)
            }

            assertContentEquals(bytes, it.getBytes(0, 5))
            assertContentEquals(bytes.takeLast(4).toByteArray(), it.getBytes(1, 4))
            assertContentEquals(bytes.takeLast(3).toByteArray(), it.getBytes(2, 3))
            assertContentEquals(bytes.takeLast(2).toByteArray(), it.getBytes(3, 2))
            assertContentEquals(bytes.takeLast(1).toByteArray(), it.getBytes(4, 1))
            assertContentEquals(byteArrayOf(), it.getBytes(5, 0))
        }
    }

    @Test
    fun memorySuppliedToMakeDataWithoutCopyDoesntGetCleanedUp() = runTest {
        val bytes = byteArrayOf(1, 2, 3, 4, 5)
        val originalData = Data.makeFromBytes(bytes)
        val originalDataPtr = originalData._ptr

        // `use()` calls `close()` in the end
        val copiedData = Data.makeWithoutCopy(
            memoryAddr = originalData.writableData(),
            length = 5,
            underlyingMemoryOwner = originalData
        ).use {
            assertNotEquals(NullPointer, it._ptr)
            assertContentEquals(bytes, it.bytes)
            it
        }

        assertEquals(NullPointer, copiedData._ptr)
        assertEquals(originalDataPtr, originalData._ptr)

        assertContentEquals(
            bytes,
            originalData.bytes,
            message = "Memory is not expected to get cleaned up because Data.makeWithoutCopy uses NoopReleaseProc as a ReleaseProc"
        )
    }

    @Test
    fun canMakeUninitialized() = runTest {
        val data = Data.makeUninitialized(100)
        assertEquals(100, data.bytes.size)

        val imageInfo = ImageInfo.makeN32Premul(5, 5)
        val pixmap = Pixmap.make(imageInfo, data, imageInfo.minRowBytes)
        val surface = Surface.makeRasterDirect(pixmap)

        surface.canvas.clear(Color.WHITE)
        // check that data contains all white pixels
        assertContentEquals(
            expected = ByteArray(100) { -1 },
            actual = data.bytes
        )

        surface.canvas.clear(Color.BLACK)
        // check that data contains all black pixels
        assertContentEquals(
            // every 4th byte is -1, the rest is 0 for Color.BLACK
            expected = ByteArray(100) { if ((it + 1) % 4 == 0) -1 else 0 },
            actual = data.bytes
        )
    }
}
