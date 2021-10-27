package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionTest {
    @Test
    fun canGetBounds() {
        val bounds = IRect(10, 10, 30, 30)
        val region = Region().apply { setRect(bounds) }

        assertEquals(bounds, region.bounds)
    }

    @Test
    fun canSetRects() {
        val bounds = IRect(10, 10, 30, 30)
        val region = Region().apply {
            setRects(arrayOf(
                IRect(10, 10, 25, 25),
                IRect(15, 15, 30, 30),
            ))
        }

        assertEquals(bounds, region.bounds)
    }
}