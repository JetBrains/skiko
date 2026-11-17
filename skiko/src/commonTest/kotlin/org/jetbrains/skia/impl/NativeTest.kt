package org.jetbrains.skia.impl

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Drawable
import org.jetbrains.skia.Font
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NativeTest {
    @Test
    fun companionInitializationSupportsNativeCallbacks() {
        assertTrue(Native.Companion.toString().isNotEmpty())

        val expectedBounds = Rect(1f, 2f, 5f, 8f)
        var boundsCalls = 0
        val drawable = object : Drawable() {
            override fun onDraw(canvas: Canvas?) {}

            override fun onGetBounds(): Rect {
                boundsCalls++
                return expectedBounds
            }
        }
        drawable.use {
            assertEquals(expectedBounds, it.bounds)
            assertEquals(1, boundsCalls)
        }
    }

    @Test
    fun initializedNativeRejectsNullPeerAndAllowsFontConstruction() {
        val failure = assertFailsWith<RuntimeException> {
            object : Native(Native.NullPointer) {}
        }
        assertEquals("Can't wrap nullptr", failure.message)
        Font().use { font ->
            assertNotNull(font.typeface).use { empty ->
                assertEquals(0, empty.glyphsCount)
            }
        }
    }
}
