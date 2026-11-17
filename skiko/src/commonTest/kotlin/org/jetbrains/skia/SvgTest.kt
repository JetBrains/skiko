package org.jetbrains.skia

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skia.svg.*
import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skia.tests.makeFromResource
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
                val pixels = renderSvg(withoutFonts, 64, 64)
                assertEquals(Color.TRANSPARENT, pixels.getColor(32, 32), "constructor draws no text")
                assertEquals(Color.GREEN, pixels.getColor(1, 32), "shapes still draw")
            }

            val built = SVGDOM.Builder().use { builder ->
                builder.setFontManager(fontMgr).buildFromData(data)
            }
            assertNotNull(built).use { withFonts ->
                val pixels = renderSvg(withFonts, 64, 64)
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
                assertEquals(Color.RED, renderSvg(withFonts, 64, 64).getColor(32, 32))
            }
        }
    }

    @Test
    fun defaultFontManagerDrawsTextWhenNoFamilyIsSet() = runTest {
        // No font-family, and no other element: every pixel is text. The default face differs
        // per platform, so compare the whole bitmap with the same SVG drawn by a manager that
        // has no face, instead of sampling one pixel of one glyph.
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
            val blank = assertNotNull(assertNotNull(empty).use { renderSvg(it, 64, 64).readPixels() })
            val drawn = assertNotNull(assertNotNull(system).use { renderSvg(it, 64, 64).readPixels() })
            // The SVG has no shape, so a manager with no face draws nothing. The default face
            // covers some of those pixels; where it covers them depends on the platform.
            assertTrue(blank.all { it == 0.toByte() }, "no face draws no text")
            assertFalse(blank.contentEquals(drawn), "default face draws the text")
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
                assertEquals(Color.TRANSPARENT, renderSvg(dom, 64, 64).getColor(32, 32))
            }
            assertNotNull(drawn).use { dom ->
                assertEquals(Color.RED, renderSvg(dom, 64, 64).getColor(32, 32))
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

    @Test
    fun embeddedBitmapIsInvisibleWithoutAResourceProvider() {
        // A 2x2 red PNG, placed at 1,1 in a 4x4 SVG. The constructor installs no provider, so the
        // image must stay transparent while a shape in the same document still draws.
        val xml = svgWithImage(pngDataUri(solidPng(2, 2, Color.RED)), x = 1, y = 1, width = 2, height = 2) +
            """<rect x="0" y="0" width="4" height="1" fill="#00ff00"/>"""
        val wrapped = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"4\" height=\"4\">$xml</svg>"
        Data.makeFromBytes(wrapped.encodeToByteArray()).use { data ->
            SVGDOM(data).use { dom ->
                val pixels = renderSvg(dom, 4, 4)
                assertEquals(Color.TRANSPARENT, pixels.getColor(1, 2), "constructor draws no embedded bitmap")
                assertEquals(Color.GREEN, pixels.getColor(0, 0), "shapes still draw")
            }
        }
    }

    @Test
    fun dataUriPngFillsItsViewport() {
        // This Skia reads the image IRI from xlink:href only. A plain href is ignored.
        val png = pngDataUri(solidPng(2, 2, Color.RED))
        val ignored = renderWithDataUri(svgDocument(svgWithImage(png, x = 0, y = 0, width = 2, height = 2, attribute = "href")), 2, 2)
        assertEquals(Color.TRANSPARENT, ignored.getColor(0, 0), "plain href is not an image IRI on this Skia")

        val xml = svgDocument(svgWithImage(png, x = 1, y = 1, width = 2, height = 2))
        val pixels = renderWithDataUri(xml, 4, 4)
        assertEquals(Color.RED, pixels.getColor(1, 1))
        assertEquals(Color.RED, pixels.getColor(2, 2))
        assertEquals(Color.TRANSPARENT, pixels.getColor(0, 0), "image does not paint outside its viewport")
        assertEquals(Color.TRANSPARENT, pixels.getColor(3, 3))
    }

    @Test
    fun xlinkHrefPngFillsItsBox() {
        val png = pngDataUri(solidPng(2, 2, Color.BLUE))
        val xlink = renderWithDataUri(svgDocument(svgWithImage(png, x = 0, y = 0, width = 2, height = 2)), 2, 2)
        assertEquals(Color.BLUE, xlink.getColor(0, 0))
        assertEquals(Color.BLUE, xlink.getColor(1, 1))
    }

    @Test
    fun jpegAndWebpDataUrisDecode() {
        // JPEG is lossy even at quality 100, so the check is the channel that must survive, not
        // an exact Color.GREEN. WebP of a flat color is exact.
        val jpeg = jpegDataUri(solidImage(8, 8, Color.GREEN))
        val webp = webpDataUri(solidImage(2, 2, Color.BLUE))
        val jpegPixels = renderWithDataUri(svgDocument(svgWithImage(jpeg, 0, 0, 8, 8)), 8, 8)
        val webpPixels = renderWithDataUri(svgDocument(svgWithImage(webp, 0, 0, 2, 2)), 2, 2)
        val jpegColor = jpegPixels.getColor(4, 4)
        assertEquals(255, Color.getA(jpegColor), "jpeg is opaque")
        assertEquals(255, Color.getG(jpegColor), "jpeg keeps the green channel")
        check(Color.getR(jpegColor) < 16) { "jpeg red leaked: ${Color.getR(jpegColor)}" }
        check(Color.getB(jpegColor) < 16) { "jpeg blue leaked: ${Color.getB(jpegColor)}" }
        assertEquals(Color.BLUE, webpPixels.getColor(0, 0))
        assertEquals(Color.BLUE, webpPixels.getColor(1, 1))
    }

    @Test
    fun scaledImageFillsTheDeclaredViewport() {
        // A 2x2 source stretched across a 6x4 viewport. Linear sampling blends the edge; the
        // center of the viewport is the source color, and the pixel just outside stays empty.
        val xml = svgDocument(svgWithImage(pngDataUri(solidPng(2, 2, Color.RED)), x = 1, y = 1, width = 6, height = 4))
        val pixels = renderWithDataUri(xml, 8, 6)
        assertEquals(Color.RED, pixels.getColor(4, 3), "center of the stretched viewport")
        assertEquals(Color.TRANSPARENT, pixels.getColor(0, 3), "left of the viewport")
        assertEquals(Color.TRANSPARENT, pixels.getColor(7, 3), "right of the viewport")
    }

    @Test
    fun translatedImageLandsAtTheTransformedOrigin() {
        val xml = svgDocument(
            """<g transform="translate(2,3)">""" +
                svgWithImage(pngDataUri(solidPng(2, 2, Color.RED)), x = 0, y = 0, width = 2, height = 2) +
                "</g>"
        )
        val pixels = renderWithDataUri(xml, 6, 6)
        assertEquals(Color.TRANSPARENT, pixels.getColor(1, 4), "left of the translated image")
        assertEquals(Color.RED, pixels.getColor(2, 3))
        assertEquals(Color.RED, pixels.getColor(3, 4))
        assertEquals(Color.TRANSPARENT, pixels.getColor(4, 4), "right of the translated image")
    }

    @Test
    fun imageOpacityScalesTheBitmapAlpha() {
        // Skia applies opacity from a parent group. An opacity attribute on <image> itself is not
        // applied by this build, so the group is the case the renderer actually honors.
        val opaque = pngDataUri(solidPng(2, 2, Color.RED))
        val onImage = renderWithDataUri(
            svgDocument(svgWithImage(opaque, x = 0, y = 0, width = 2, height = 2, opacity = "0.5")),
            2,
            2
        )
        val grouped = renderWithDataUri(
            svgDocument("<g opacity=\"0.5\">" + svgWithImage(opaque, x = 0, y = 0, width = 2, height = 2) + "</g>"),
            2,
            2
        )
        assertEquals(255, Color.getA(onImage.getColor(0, 0)), "opacity on <image> is ignored")
        val color = grouped.getColor(0, 0)
        assertEquals(Color.RED and 0x00FFFFFF, color and 0x00FFFFFF, "premultiplied red stays red")
        val alpha = Color.getA(color)
        assertNotEquals(0, alpha)
        assertNotEquals(255, alpha)
    }

    @Test
    fun zeroWidthImageIsNotDrawn() {
        val xml = svgDocument(svgWithImage(pngDataUri(solidPng(2, 2, Color.RED)), x = 0, y = 0, width = 0, height = 2))
        val pixels = renderWithDataUri(xml, 2, 2)
        assertEquals(Color.TRANSPARENT, pixels.getColor(0, 0))
        assertEquals(Color.TRANSPARENT, pixels.getColor(1, 1))
    }

    @Test
    fun malformedEmbeddedImagesAreNotDrawn() {
        val cases = listOf(
            "data:image/png;base64,%%%not-base64%%%",
            "data:image/png," + base64(solidPng(2, 2, Color.RED)),
            "data:image/svg+xml;base64," + base64("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"2\" height=\"2\"><rect width=\"2\" height=\"2\" fill=\"red\"/></svg>".encodeToByteArray()),
            "https://example.invalid/icon.png",
            "images/icon.png"
        )
        for (href in cases) {
            val pixels = renderWithDataUri(
                svgDocument(svgWithImage(href, x = 0, y = 0, width = 2, height = 2)),
                2,
                2
            )
            assertEquals(Color.TRANSPARENT, pixels.getColor(0, 0), href)
            assertEquals(Color.TRANSPARENT, pixels.getColor(1, 1), href)
        }
    }

    @Test
    fun domKeepsTheProviderAfterTheCallerClosesIt() {
        val xml = svgDocument(svgWithImage(pngDataUri(solidPng(2, 2, Color.RED)), x = 0, y = 0, width = 2, height = 2))
        val provider = SVGResourceProvider.makeDataURI()
        val built = Data.makeFromBytes(xml.encodeToByteArray()).use { data ->
            SVGDOM.Builder().use { builder ->
                builder.setResourceProvider(provider).buildFromData(data)
            }
        }
        provider.close()
        assertNotNull(built).use { dom ->
            assertEquals(Color.RED, renderSvg(dom, 2, 2).getColor(1, 1))
        }
    }

    private fun renderWithDataUri(xml: String, width: Int, height: Int): Bitmap {
        return Data.makeFromBytes(xml.encodeToByteArray()).use { data ->
            val dom = SVGDOM.Builder().use { builder ->
                builder.setResourceProvider(SVGResourceProvider.makeDataURI()).buildFromData(data)
            }
            assertNotNull(dom).use { renderSvg(it, width, height) }
        }
    }

    private fun renderSvg(dom: SVGDOM, width: Int, height: Int): Bitmap {
        return Surface.makeRasterN32Premul(width, height).use { surface ->
            dom.render(surface.canvas)
            Bitmap.makeFromImage(surface.makeImageSnapshot())
        }
    }

    private fun svgDocument(body: String): String =
        "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"32\" height=\"32\">$body</svg>"

    private fun svgWithImage(
        href: String,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        attribute: String = "xlink:href",
        opacity: String? = null
    ): String {
        val opacityAttr = if (opacity == null) "" else " opacity=\"$opacity\""
        return "<image x=\"$x\" y=\"$y\" width=\"$width\" height=\"$height\" $attribute=\"$href\"$opacityAttr/>"
    }

    private fun solidImage(width: Int, height: Int, color: Int): Image {
        return Surface.makeRasterN32Premul(width, height).use { surface ->
            surface.canvas.drawRect(Rect.makeWH(width.toFloat(), height.toFloat()), Paint().apply { this.color = color })
            surface.makeImageSnapshot()
        }
    }

    private fun solidPng(width: Int, height: Int, color: Int): ByteArray {
        return solidImage(width, height, color).use { image ->
            image.encodeToData(EncodedImageFormat.PNG, pngCompressionLevel = 0)!!.bytes
        }
    }

    private fun pngDataUri(png: ByteArray): String = "data:image/png;base64,${base64(png)}"

    private fun jpegDataUri(image: Image): String {
        val bytes = image.encodeToData(EncodedImageFormat.JPEG, quality = 100)!!.bytes
        return "data:image/jpeg;base64,${base64(bytes)}"
    }

    private fun webpDataUri(image: Image): String {
        val bytes = image.encodeToData(EncodedImageFormat.WEBP, quality = 100)!!.bytes
        return "data:image/webp;base64,${base64(bytes)}"
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun base64(bytes: ByteArray): String = Base64.encode(bytes)
}
