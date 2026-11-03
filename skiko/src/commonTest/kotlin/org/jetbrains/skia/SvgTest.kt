package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skia.svg.*
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skiko.KotlinBackend
import org.jetbrains.skiko.kotlinBackend
import org.jetbrains.skiko.tests.runTest

class SvgTest {
    @Test
    fun svg_smoke() {
        val svgText = """
            <svg version="1.1"
                 width="300" height="200"
                 xmlns="http://www.w3.org/2000/svg">

              <rect width="100%" height="100%" fill="red" />

              <circle cx="150" cy="100" r="80" fill="green" />

              <text x="150" y="125" font-size="60" text-anchor="middle" fill="white">SVG</text>

            </svg>
        """.trimIndent()
        val data = Data.makeFromBytes(svgText.encodeToByteArray())
        val dom = SVGDOM(data)
        require(!dom.isClosed)
        dom.setContainerSize(Point(100f, 100f))
        dom.setContainerSize(101f, 101f)
        require(dom.root != null)
        val e = dom.root!!
        assertEquals(SVGLengthUnit.NUMBER, e.x.unit)
        assertEquals(SVGLengthUnit.NUMBER, e.y.unit)
        assertEquals(SVGLengthUnit.NUMBER, e.width.unit)
        assertEquals(SVGLengthUnit.NUMBER, e.height.unit)
        assertEquals(null, e.viewBox)
        assertEquals(SVGTag.SVG, e.tag)
        e.viewBox = Rect(0f, 1f, 100f, 200f)
        assertCloseEnough(Rect(0f, 1f, 100f, 200f), e.viewBox!!)
        val aspectRatio =
            SVGPreserveAspectRatio(SVGPreserveAspectRatioAlign.XMIN_YMIN, SVGPreserveAspectRatioScale.MEET)
        e.preserveAspectRatio = aspectRatio
        assertEquals(aspectRatio, e.preserveAspectRatio)
        require(e.getIntrinsicSize(SVGLengthContext(100f, 100f)).x == 300f)
        e.viewBox = Rect.makeXYWH(0f, 1f, 2f, 3f)
        require(e.viewBox == Rect.makeXYWH(0f, 1f, 2f, 3f))
    }

    @Test
    fun SvgLength() {
        val l = SVGLength(123f)

        assertEquals(123f, l.value)
        assertEquals(SVGLengthUnit.NUMBER, l.unit)

        val lpx = l.withUnit(SVGLengthUnit.PX)
        assertEquals(123f, lpx.value)
        assertEquals(SVGLengthUnit.PX, lpx.unit)

        val newLpx = lpx.withValue(321f)
        assertEquals(321f, newLpx.value)
        assertEquals(SVGLengthUnit.PX, newLpx.unit)
    }

    @Test
    fun rootLengthsRoundTripValuesAndUnits() {
        val xml = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"10\" height=\"20\"/>"
        Data.makeFromBytes(xml.encodeToByteArray()).use { data ->
            SVGDOM(data).use { dom ->
                val root = assertNotNull(dom.root)
                root.use {
                    val x = SVGLength(12f, SVGLengthUnit.PX)
                    val y = SVGLength(23f, SVGLengthUnit.PERCENTAGE)
                    val width = SVGLength(34f, SVGLengthUnit.PERCENTAGE)
                    val height = SVGLength(45f, SVGLengthUnit.PX)
                    root.x = x
                    root.y = y
                    root.width = width
                    root.height = height
                    assertEquals(x, root.x)
                    assertEquals(y, root.y)
                    assertEquals(width, root.width)
                    assertEquals(height, root.height)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun containerSizeRoundTrips() {
        val xml = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>"
        Data.makeFromBytes(xml.encodeToByteArray()).use { data ->
            SVGDOM(data).use { dom ->
                dom.setContainerSize(123f, 45f)
                assertEquals(Point(123f, 45f), dom.containerSize)
                dom.setContainerSize(Point(67f, 89f))
                assertEquals(Point(67f, 89f), dom.containerSize)
            }
        }
    }

    @Test
    fun textIsInvisibleWithoutAFontManagerAndDrawnWithOne() = runTest {
        // A glyph of a 64px face is far larger than the red border, so a pixel inside the
        // letter cannot be explained by the rect.
        val xml = """
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64">
              <rect x="0" y="0" width="64" height="64" fill="none" stroke="#00ff00" stroke-width="4"/>
              <text x="0" y="52" font-family="JetBrains Mono" font-size="64" fill="#ff0000">M</text>
            </svg>
        """.trimIndent()
        val typeface = Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf")
        val fontMgr = TypefaceFontProvider().apply { registerTypeface(typeface) }

        Data.makeFromBytes(xml.encodeToByteArray()).use { data ->
            SVGDOM(data).use { withoutFonts ->
                val pixels = renderSvg(withoutFonts)
                assertEquals(Color.TRANSPARENT, pixels.getColor(32, 32), "constructor draws no text")
                assertEquals(Color.GREEN, pixels.getColor(1, 32), "shapes still draw")
            }

            val built = SVGDOM.Builder().use { builder ->
                builder.setFontManager(fontMgr).buildFromData(data)
            }
            assertNotNull(built).use { withFonts ->
                val pixels = renderSvg(withFonts)
                assertEquals(Color.RED, pixels.getColor(32, 32), "registered face draws the glyph")
                assertEquals(Color.GREEN, pixels.getColor(1, 32), "shapes are unchanged")
            }

            // The DOM keeps its own reference: closing the builder and the caller's manager
            // must not drop the face the DOM still renders with.
            val detached = SVGDOM.Builder().use { builder ->
                builder.setFontManager(fontMgr).buildFromData(data)
            }
            fontMgr.close()
            typeface.close()
            assertNotNull(detached).use { withFonts ->
                assertEquals(Color.RED, renderSvg(withFonts).getColor(32, 32))
            }
        }
    }

    @Test
    fun defaultFontManagerDrawsTextWhenNoFamilyIsSet() = runTest {
        // No font-family: Skia asks the manager for its default face. An empty manager has
        // none, so a drawn pixel is FontMgr.default and not a hardcoded face.
        val xml = """
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64">
              <text x="0" y="52" font-size="64" fill="#0000ff">M</text>
            </svg>
        """.trimIndent()
        Data.makeFromBytes(xml.encodeToByteArray()).use { data ->
            val empty = SVGDOM.Builder().use { builder ->
                builder.setFontManager(TypefaceFontProvider()).buildFromData(data)
            }
            val system = SVGDOM.Builder().use { builder ->
                builder.setFontManager(FontMgr.default).buildFromData(data)
            }
            assertNotNull(empty).use { dom ->
                assertEquals(Color.TRANSPARENT, renderSvg(dom).getColor(32, 32))
            }
            assertNotNull(system).use { dom ->
                val color = renderSvg(dom).getColor(32, 32)
                assertEquals(0xFF, Color.getA(color), "default face draws the glyph")
                assertNotEquals(Color.TRANSPARENT, color)
            }
        }
    }

    @Test
    fun unknownFamilyFallsBackToTheManagersDefaultFace() = runTest {
        // Skia tries the named family, then the manager's default face. A provider whose only
        // face is not the named one still draws, with that face; an empty provider draws nothing.
        val xml = """
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64">
              <text x="0" y="52" font-family="No Such Family" font-size="64" fill="#ff0000">M</text>
            </svg>
        """.trimIndent()
        val empty = TypefaceFontProvider()
        val fallback = TypefaceFontProvider().apply {
            registerTypeface(Typeface.makeFromResource("./fonts/JetBrainsMono-Regular.ttf"))
        }
        Data.makeFromBytes(xml.encodeToByteArray()).use { data ->
            val none = SVGDOM.Builder().use { builder ->
                builder.setFontManager(empty).buildFromData(data)
            }
            val drawn = SVGDOM.Builder().use { builder ->
                builder.setFontManager(fallback).buildFromData(data)
            }
            assertNotNull(none).use { dom ->
                assertEquals(Color.TRANSPARENT, renderSvg(dom).getColor(32, 32))
            }
            assertNotNull(drawn).use { dom ->
                assertEquals(Color.RED, renderSvg(dom).getColor(32, 32))
            }
        }
    }

    @Test
    fun builderRejectsAStreamThatIsNotSvg() {
        Data.makeFromBytes("not svg".encodeToByteArray()).use { data ->
            SVGDOM.Builder().use { builder ->
                assertNull(builder.setFontManager(FontMgr.default).buildFromData(data))
            }
        }
    }

    private fun renderSvg(dom: SVGDOM): Bitmap {
        return Surface.makeRasterN32Premul(64, 64).use { surface ->
            dom.render(surface.canvas)
            Bitmap.makeFromImage(surface.makeImageSnapshot())
        }
    }
}
