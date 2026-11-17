package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaintTest {
    @Test
    fun drawingOptionsRoundTrip() {
        Paint().use { paint ->
            paint.isAntiAlias = true
            paint.isDither = true
            paint.mode = PaintMode.STROKE
            paint.strokeWidth = 7f
            paint.strokeMiter = 6f
            paint.strokeCap = PaintStrokeCap.SQUARE
            paint.strokeJoin = PaintStrokeJoin.BEVEL
            paint.blendMode = BlendMode.MULTIPLY

            assertTrue(paint.isAntiAlias)
            assertTrue(paint.isDither)
            assertEquals(PaintMode.STROKE, paint.mode)
            assertEquals(7f, paint.strokeWidth)
            assertEquals(6f, paint.strokeMiter)
            assertEquals(PaintStrokeCap.SQUARE, paint.strokeCap)
            assertEquals(PaintStrokeJoin.BEVEL, paint.strokeJoin)
            assertEquals(BlendMode.MULTIPLY, paint.blendMode)

            paint.isAntiAlias = false
            paint.isDither = false
            assertFalse(paint.isAntiAlias)
            assertFalse(paint.isDither)
        }
    }

    @Test
    fun cloneAndReset() {
        Paint().use { paint ->
            paint.color = Color.RED
            paint.strokeWidth = 5f
            paint.isDither = true
            paint.makeClone().use { clone ->
                assertEquals(paint, clone)
                clone.color = Color.BLUE
                assertNotEquals(paint, clone)
                assertEquals(Color.RED, paint.color)
                clone.reset()
                assertFalse(clone.isAntiAlias)
                Paint().use { defaults ->
                    defaults.isAntiAlias = false
                    assertEquals(defaults, clone)
                }
            }
        }
    }

    @Test
    fun effectsRoundTripAndClear() {
        Paint().use { paint ->
            assertNull(paint.shader)
            assertNull(paint.colorFilter)
            assertNull(paint.imageFilter)
            assertNull(paint.maskFilter)
            assertNull(paint.pathEffect)
            assertNull(paint.blender)

            Shader.makeColor(Color.RED).use { shader ->
                paint.shader = shader
                assertNotNull(paint.shader).use { assertEquals(shader, it) }
            }
            ColorFilter.makeLighting(Color.WHITE, Color.RED).use { filter ->
                paint.colorFilter = filter
                assertNotNull(paint.colorFilter).use { assertEquals(filter, it) }
            }
            ImageFilter.makeBlur(2f, 2f, FilterTileMode.DECAL).use { filter ->
                paint.imageFilter = filter
                assertNotNull(paint.imageFilter).use { assertEquals(filter, it) }
            }
            MaskFilter.makeBlur(FilterBlurMode.NORMAL, 2f).use { filter ->
                paint.maskFilter = filter
                assertNotNull(paint.maskFilter).use { assertEquals(filter, it) }
            }
            PathEffect.makeDash(floatArrayOf(2f, 3f), 0f).use { effect ->
                paint.pathEffect = effect
                assertNotNull(paint.pathEffect).use { assertEquals(effect, it) }
            }
            Blender.makeMode(BlendMode.SRC).use { blender ->
                paint.blender = blender
                assertNotNull(paint.blender).use { assertEquals(blender, it) }
                assertEquals(BlendMode.SRC, paint.blendMode)
            }

            paint.shader = null
            paint.colorFilter = null
            paint.imageFilter = null
            paint.maskFilter = null
            paint.pathEffect = null
            paint.blender = null
            assertNull(paint.shader)
            assertNull(paint.colorFilter)
            assertNull(paint.imageFilter)
            assertNull(paint.maskFilter)
            assertNull(paint.pathEffect)
            assertNull(paint.blender)
        }
    }

    @Test
    fun nothingToDrawDependsOnAlphaAndBlendMode() {
        Paint().use { paint ->
            assertFalse(paint.hasNothingToDraw())
            paint.color = Color.TRANSPARENT
            assertTrue(paint.hasNothingToDraw())
            paint.blendMode = BlendMode.SRC
            assertFalse(paint.hasNothingToDraw())
            paint.blendMode = BlendMode.DST
            assertTrue(paint.hasNothingToDraw())
        }
    }

    @Test
    fun color4f() {
        val paint = Paint()
        val expected = Color4f(0.2f, 0.4f, 0.8f, 1.0f)
        paint.setColor4f(expected, colorSpace = ColorSpace.sRGB)
        assertCloseEnough(expected, paint.color4f)
    }
}
