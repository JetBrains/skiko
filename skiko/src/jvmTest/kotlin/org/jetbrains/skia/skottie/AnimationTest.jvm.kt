package org.jetbrains.skiko.tests.org.jetbrains.skia.skottie

import org.jetbrains.skia.Data
import org.jetbrains.skia.Point
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.skottie.AnimationBuilder
import org.jetbrains.skia.skottie.buildFromFile
import org.jetbrains.skia.skottie.makeFromFile
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.resourcePath
import org.jetbrains.skiko.tests.runTest
import org.junit.Test
import kotlin.test.assertEquals


class AnimationTestJVM {
    @Test
    fun canCreateFromFile() {
        val animation = Animation.makeFromFile(
            resourcePath("./skottie/test_animation01.json")
        )

        assertEquals("1.42.0", animation.version)
        assertEquals(Point(32.0f, 32.0f), animation.size)
        assertEquals(60.0f, animation.fPS)
        assertEquals(3.0f, animation.duration)
        assertEquals(0.0f, animation.inPoint)
        assertEquals(180.0f, animation.outPoint)
    }

    @Test
    fun canCreateFromFileWithBuilder() {
        val animation = AnimationBuilder().buildFromFile(
            resourcePath("./skottie/test_animation01.json")
        )

        assertEquals("1.42.0", animation.version)
        assertEquals(Point(32.0f, 32.0f), animation.size)
        assertEquals(60.0f, animation.fPS)
        assertEquals(3.0f, animation.duration)
        assertEquals(0.0f, animation.inPoint)
        assertEquals(180.0f, animation.outPoint)
    }

    @Test
    fun canCreateFromData() = runTest {
        val animation = Animation.makeFromData(
            Data.makeFromResource("./skottie/test_animation01.json")
        )

        assertEquals("1.42.0", animation.version)
        assertEquals(Point(32.0f, 32.0f), animation.size)
        assertEquals(60.0f, animation.fPS)
        assertEquals(3.0f, animation.duration)
        assertEquals(0.0f, animation.inPoint)
        assertEquals(180.0f, animation.outPoint)
    }

    @Test
    fun canCreateFromDataWithBuilder() = runTest {
        val animation = AnimationBuilder().buildFromData(
            Data.makeFromResource("./skottie/test_animation01.json")
        )

        assertEquals("1.42.0", animation.version)
        assertEquals(Point(32.0f, 32.0f), animation.size)
        assertEquals(60.0f, animation.fPS)
        assertEquals(3.0f, animation.duration)
        assertEquals(0.0f, animation.inPoint)
        assertEquals(180.0f, animation.outPoint)
    }
}