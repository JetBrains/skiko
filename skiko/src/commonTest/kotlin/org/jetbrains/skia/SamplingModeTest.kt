package org.jetbrains.skia

import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SamplingModeTest {

    @Test
    fun packFilterMipmap() = runTest {
        FilterMode.entries.forEach { filterMode ->
            MipmapMode.entries.forEach { mipmapMode ->
                val samplingMode = FilterMipmap(filterMode, mipmapMode)

                val long = samplingMode._pack() // we treat it as expected since it was used initially
                val l1 = (long shr 32).toInt()
                val l2 = (long and 0x00000000FFFFFFFF).toInt()

                assertEquals(filterMode.ordinal, l1)
                assertEquals(mipmapMode.ordinal, l2)

                assertEquals(l1, samplingMode._packedInt1())
                assertEquals(l2, samplingMode._packedInt2())
            }
        }
    }

    @Test
    fun packCubicResampler() = runTest {
        val floats = floatArrayOf(0f, 0.5f, 0.9f, 1f / 3, 2f / 3, 1f)

        floats.forEach { b ->
            floats.forEach { c ->
                val samplingMode = CubicResampler(b = b, c = c)

                val long = samplingMode._pack() // we treat it as expected since it was used initially
                assertEquals(1.toULong(), long.toULong() shr 63)

                val bBits = b.toBits()
                val cBits = c.toBits()

                assertEquals(((long and 0x7FFFFFFFFFFFFFFF) shr 32).toInt(), bBits)
                assertEquals((long and 0xFFFFFFFF).toInt() , cBits)

                val int1 = samplingMode._packedInt1()
                val int2 = samplingMode._packedInt2()
                assertEquals(long, (int1.toULong() shl 32 or int2.toULong()).toLong())
            }
        }
    }
}
