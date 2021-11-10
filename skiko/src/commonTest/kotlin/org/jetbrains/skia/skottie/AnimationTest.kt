package org.jetbrains.skia.skottie

import org.jetbrains.skia.Point
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimationTest {
    @Test
    fun canCreateFromString() {
        val animation = Animation.makeFromString(
            """{"nm": "Test","v": "1.42.0","ip": 0,"op": 180,"fr": 60,"w": 32,"h": 32,"layers": []}"""
        )

        assertEquals("1.42.0", animation.version)
        assertEquals(Point(32.0f, 32.0f), animation.size)
        assertEquals(60.0f, animation.fPS)
        assertEquals(3.0f, animation.duration)
        assertEquals(0.0f, animation.inPoint)
        assertEquals(180.0f, animation.outPoint)
    }

    @Test
    fun canCreateFromStringBuilder() {
        val animation = AnimationBuilder().buildFromString(
            """{"nm": "Test","v": "1.42.0","ip": 0,"op": 180,"fr": 60,"w": 32,"h": 32,"layers": []}"""
        )

        assertEquals("1.42.0", animation.version)
        assertEquals(Point(32.0f, 32.0f), animation.size)
        assertEquals(60.0f, animation.fPS)
        assertEquals(3.0f, animation.duration)
        assertEquals(0.0f, animation.inPoint)
        assertEquals(180.0f, animation.outPoint)
    }
}