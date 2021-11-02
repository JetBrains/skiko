package org.jetbrains.skiko.tests.org.jetbrains.skia.skottie

import org.jetbrains.skia.Point
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.skottie.makeFromFile
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.resourcePath
import org.junit.Test
import kotlin.test.assertEquals


class AnimationTestJVM {
    @Test
    fun canCreateFromFile() {
        if (kotlinBackend == KotlinBackend.JS) {
            return
        }

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
}