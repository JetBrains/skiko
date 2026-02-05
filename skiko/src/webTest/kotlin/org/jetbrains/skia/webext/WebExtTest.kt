package org.jetbrains.skia.webext

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl._free
import org.jetbrains.skia.impl.skia_memGetByte
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTimedValue

class WebExtTest {

    @Test
    fun canCopyArrayBufferToSkikoMemory() = runTest {
        val bytes = ByteArray(16_000_000) { (it and 0xFF).toByte() }
        val ba = createInt8ArrayToCopy(bytes.size)
        for (i in bytes.indices) ba[i] = bytes[i]

        val ptr = measureTimedValue<NativePointer> { copyBufferToSkiko(ba.buffer) }
        println("copyBufferToSkiko took ${ptr.duration}\n")

        // Sanity check
        assertEquals(bytes[2], skia_memGetByte(ptr.value + 2))
        assertEquals(bytes[228], skia_memGetByte(ptr.value + 228))
        assertEquals(bytes[5555], skia_memGetByte(ptr.value + 5555))
        assertEquals(bytes[10_000_000], skia_memGetByte(ptr.value + 10_000_000))
        assertEquals(bytes[14_000_000], skia_memGetByte(ptr.value + 14_000_000))
        _free(ptr.value)
    }

    @Test
    fun canCreateBitmapAndSetPixelsFromArrayBuffer() = runTest {
        val width = 100
        val height = 100
        val bytesPerPixel = 4 // RGBA_8888
        val bytes = ByteArray(width * height * bytesPerPixel) { i ->
            val pixelIndex = i / 4
            val x = pixelIndex % width
            val y = pixelIndex / width
            when (i % 4) {
                0 -> x.toByte()      // R: gradient by X
                1 -> y.toByte()      // G: gradient by Y
                2 -> (x + y).toByte() // B: combined gradient
                3 -> 255.toByte()    // A: fully opaque
                else -> 0
            }
        }

        val ba = createInt8ArrayToCopy(bytes.size)
        for (i in bytes.indices) ba[i] = bytes[i]

        val info = ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
        val bitmap = Bitmap()

        val success = bitmap.installPixelsFromArrayBuffer(info, ba.buffer, width * bytesPerPixel)

        @Suppress("RedundantIf") // Weird? Yes. on js success is not a boolean, it's = 1
        assertEquals(true, if (success) true else false)

        // Check some specific pixels
        // (0,0) -> R=0, G=0, B=0, A=255 -> 0xFF000000
        assertEquals(0xFF000000.toInt(), bitmap.getColor(0, 0))

        // (50, 0) -> R=50, G=0, B=50, A=255 -> 0xFF320032 (50 is 0x32)
        assertEquals(0xFF320032.toInt(), bitmap.getColor(50, 0))

        // (0, 50) -> R=0, G=50, B=50, A=255 -> 0xFF003232
        assertEquals(0xFF003232.toInt(), bitmap.getColor(0, 50))

        // (99, 99) -> R=99, G=99, B=198, A=255 -> 0xFF6363C6 (99 is 0x63, 198 is 0xC6)
        assertEquals(0xFF6363C6.toInt(), bitmap.getColor(99, 99))
    }
}

private fun createInt8ArrayToCopy(length: Int): Int8ArrayInternal =
    js("new Int8Array(length)")

private external interface Int8ArrayInternal {
    val buffer: WebArrayBufferExt
}

private operator fun Int8ArrayInternal.set(index: Int, value: Byte) {
    int8ArraySet(this, index, value)
}

private fun int8ArraySet(arrayInternal: Int8ArrayInternal, index: Int, value: Byte) {
    js("arrayInternal[index] = value;")
}