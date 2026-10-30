package org.jetbrains.skia.skottie

import org.jetbrains.skia.Color
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Point
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AnimationTest {
    @Test
    fun canCreateFromString() = runTest {
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
    fun canCreateFromStringBuilder() = runTest {
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

    @Test
    fun canCreateFromData() = runTest {
        Data.makeFromBytes(animationJson.encodeToByteArray()).use { data ->
            Animation.makeFromData(data).use { animation ->
                assertEquals(Point(16f, 8f), animation.size)
                assertEquals(60f, animation.fPS)
                assertEquals(1f, animation.duration)
            }
        }
        Data.makeFromBytes("invalid json".encodeToByteArray()).use { data ->
            assertFailsWith<IllegalArgumentException> { Animation.makeFromData(data) }
        }
    }

    @Test
    fun builderCanReplaceFontManagerAndBuildFromData() = runTest {
        AnimationBuilder().use { builder ->
            assertSame(builder, builder.setFontManager(FontMgr.default))
            assertSame(builder, builder.setFontManager(null))
            Data.makeFromBytes(animationJson.encodeToByteArray()).use { data ->
                builder.buildFromData(data).use { animation ->
                    assertEquals(Point(16f, 8f), animation.size)
                    assertEquals(1f, animation.duration)
                }
            }
            Data.makeFromBytes("invalid json".encodeToByteArray()).use { data ->
                assertFailsWith<IllegalArgumentException> { builder.buildFromData(data) }
            }
        }
    }

    @Test
    fun replacingFontManagerChangesRenderedText() = runTest {
        val fontPath = if (kotlinBackend.isWeb()) "./fonts/" else "../../../../src/commonTest/resources/fonts/"
        Typeface.makeFromResource(fontPath + "Inter-Hinted-Regular.ttf").use { inter ->
            Typeface.makeFromResource(fontPath + "JetBrainsMono-Regular.ttf").use { mono ->
                TypefaceFontProvider().use { interManager ->
                    TypefaceFontProvider().use { monoManager ->
                        interManager.registerTypeface(inter, "AnimationTestFont")
                        monoManager.registerTypeface(mono, "AnimationTestFont")
                        AnimationBuilder().use { builder ->
                            fun renderText(): IntArray = builder.buildFromString(textAnimationJson).use { animation ->
                                Surface.makeRasterN32Premul(160, 64).use { surface ->
                                    surface.canvas.clear(Color.TRANSPARENT)
                                    animation.seekFrame(0f).render(surface.canvas)
                                    Pixmap().use { pixels ->
                                        assertTrue(surface.peekPixels(pixels))
                                        IntArray(160 * 64) { index -> pixels.getColor(index % 160, index / 160) }
                                    }
                                }
                            }

                            assertSame(builder, builder.setFontManager(interManager))
                            val interPixels = renderText()
                            assertTrue(interPixels.any { it != Color.TRANSPARENT })
                            assertSame(builder, builder.setFontManager(monoManager))
                            val monoPixels = renderText()
                            assertTrue(monoPixels.any { it != Color.TRANSPARENT })
                            assertFalse(interPixels.contentEquals(monoPixels))
                            assertSame(builder, builder.setFontManager(interManager))
                            assertContentEquals(interPixels, renderText())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun seekVariantsRenderTheSameFrame() = runTest {
        Animation.makeFromString(animationJson).use { animation ->
            Surface.makeRasterN32Premul(16, 8).use { surface ->
                fun assertFrame(seek: Animation.() -> Animation) {
                    animation.seek(0f)
                    surface.canvas.clear(Color.TRANSPARENT)
                    animation.render(surface.canvas)
                    Pixmap().use { pixels ->
                        assertTrue(surface.peekPixels(pixels))
                        assertEquals(Color.RED, pixels.getColor(1, 1))
                        assertEquals(Color.TRANSPARENT, pixels.getColor(10, 1))
                    }
                    assertSame(animation, seek.invoke(animation))
                    surface.canvas.clear(Color.TRANSPARENT)
                    assertSame(animation, animation.render(surface.canvas))
                    Pixmap().use { pixels ->
                        assertTrue(surface.peekPixels(pixels))
                        assertEquals(Color.TRANSPARENT, pixels.getColor(1, 1))
                        assertEquals(Color.RED, pixels.getColor(6, 1))
                    }
                }
                assertFrame { seek(0.5f) }
                assertFrame { seekFrame(30f) }
                assertFrame { seekFrameTime(0.5f) }
            }
        }
    }

    private val textAnimationJson = """
        {"v":"5.7.4","fr":60,"ip":0,"op":60,"w":160,"h":64,
         "fonts":{"list":[{"fName":"AnimationTestFont","fFamily":"AnimationTestFont","fStyle":"Regular","ascent":75}]},
         "layers":[{"ty":5,"ind":1,"ip":0,"op":60,"st":0,"sr":1,
           "ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"a":{"a":0,"k":[0,0,0]},
                 "s":{"a":0,"k":[100,100,100]},"p":{"a":0,"k":[8,48,0]}},
           "t":{"d":{"k":[{"t":0,"s":{"t":"WWWiii","f":"AnimationTestFont","s":32,
                 "j":0,"tr":0,"lh":40,"ls":0,"fc":[1,0,0]}}]}}}]}
    """.trimIndent()

    private val animationJson = """
        {"v":"5.7.4","fr":60,"ip":0,"op":60,"w":16,"h":8,"layers":[
          {"ty":1,"ind":1,"ip":0,"op":60,"st":0,"sr":1,"sw":8,"sh":8,"sc":"#ff0000",
           "ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"a":{"a":0,"k":[0,0,0]},
                 "s":{"a":0,"k":[100,100,100]},"p":{"a":1,"k":[
                   {"t":0,"s":[0,0,0],"e":[8,0,0],"o":{"x":0,"y":0},"i":{"x":1,"y":1}},
                   {"t":60,"s":[8,0,0]}]}}}
        ]}
    """.trimIndent()
}
