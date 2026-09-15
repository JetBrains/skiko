@file:Suppress("RemoveRedundantCallsOfConversionMethods")

package org.jetbrains.skia

import org.jetbrains.skia.tests.makeFromResource
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.util.assertContentSame
import org.jetbrains.skia.util.imageFromIntArray
import org.jetbrains.skiko.tests.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class CanvasTest {


    @Test
    fun surfaceReturnsNullForBitmapCanvas() {
        val bitmap = Bitmap().apply { allocPixels(ImageInfo.makeN32Premul(8, 8)) }

        assertNull(Canvas(bitmap).surface)
    }

    @Test
    fun surfaceIsBorrowedForRasterSurfaceCanvas() {
        Surface.makeRasterN32Premul(8, 8).use { surface ->
            val canvasSurface = surface.canvas.surface
            assertNotNull(canvasSurface)
            assertEquals(surface, canvasSurface)
            assertFails { canvasSurface.close() }
        }
    }

    @Test
    fun surfaceOfRenderTargetCanvasCarriesItsContext() {
        if (!TestGlContext.isAvailable()) return

        if (hostOs == OS.Linux && kotlinBackend == KotlinBackend.Native && hostArch == Arch.Arm64) {
            // TODO: fix test on Linux arm64 using EGL
            return
        }

        TestGlContext.run {
            DirectContext.makeGL().useContext { ctx ->
                val imageInfo = ImageInfo.makeN32Premul(16, 16)
                val surface = Surface.makeRenderTarget(ctx, budgeted = false, imageInfo)
                val canvasSurface = surface.canvas.surface
                assertNotNull(canvasSurface)
                assertEquals(surface, canvasSurface)
                assertEquals(ctx, canvasSurface._context)
            }
        }
    }

    @Test
    fun drawVertices() {
        val surface = Surface.makeRasterN32Premul(8, 8)

        val positions = Point.flattenArray(
            arrayOf(
                Point(0.0f, 0.0f),
                Point(4.0f, 4.0f),
                Point(8.0f, 0.0f),
                Point(4.0f, 4.0f),
                Point(0.0f, 8.0f),
                Point(8.0f, 8.0f),
            )
        )!!

        val colors = listOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW
        ).toIntArray()

        val indices = shortArrayOf(0, 1, 2, 3, 4, 5)

        surface.canvas.drawVertices(
            VertexMode.TRIANGLES,
            positions,
            colors,
            null,
            indices,
            BlendMode.DST_OVER,
            Paint()
        )

        // Hint: use surface.makeImageSnapshot().printBitmap() to print image as kotlin array
        val expected = imageFromIntArray(intArrayOf(
            0x00000000, 0xffbf2020.toInt(), 0xff9f2040.toInt(), 0xff802060.toInt(), 0xff602080.toInt(), 0xff40209f.toInt(), 0xff2020bf.toInt(), 0xff0020df.toInt(),
            0x00000000, 0x00000000, 0xff806020.toInt(), 0xff606040.toInt(), 0xff406060.toInt(), 0xff206080.toInt(), 0xff00609f.toInt(), 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0xff409f20.toInt(), 0xff209f40.toInt(), 0xff009f60.toInt(), 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xff00df20.toInt(), 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xff20ffdf.toInt(), 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0xff60bfdf.toInt(), 0xff60dfbf.toInt(), 0xff60ff9f.toInt(), 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0xff9f80df.toInt(), 0xff9f9fbf.toInt(), 0xff9fbf9f.toInt(), 0xff9fdf80.toInt(), 0xff9fff60.toInt(), 0x00000000,
            0x00000000, 0xffdf40df.toInt(), 0xffdf60bf.toInt(), 0xffdf809f.toInt(), 0xffdf9f80.toInt(), 0xffdfbf60.toInt(), 0xffdfdf40.toInt(), 0xffdfff20.toInt(),
        ), surface.width)

        assertContentSame(expected, surface.makeImageSnapshot(), 0.25)
    }

    @Test
    fun drawPoints() {
        val surface = Surface.makeRasterN32Premul(8, 8)

        surface.canvas.drawPoints(
            paint = Paint().apply {
                setStroke(true)
                setARGB(0xFF, 0xFF, 0x00, 0x00)
                strokeWidth = 2.0f
            },
            coords = arrayOf(
                Point(1.0f, 1.0f),
                Point(4.0f, 4.0f),
                Point(3.0f, 5.0f),
                Point(2.0f, 6.0f),
                Point(8.0f, 8.0f),
            )
        )

        val expected = imageFromIntArray(intArrayOf(
            0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0xffff0000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0xffff0000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0xffff0000.toInt(), 0xffff0000.toInt(), 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xffff0000.toInt(),
        ), surface.width)

        assertContentSame(expected, surface.makeImageSnapshot(), 0.25)
    }

    @Test
    fun clipsRestrictPaintedPixels() {
        val bounds = Rect(2f, 2f, 6f, 6f)
        val clips: List<(Canvas) -> Unit> = listOf(
            { canvas ->
                PathBuilder().use { builder ->
                    builder.addRect(bounds).detach().use { path -> canvas.clipPath(path, false) }
                }
            },
            { canvas -> canvas.clipRRect(RRect.makeLTRB(2f, 2f, 6f, 6f, 1f), false) },
            { canvas ->
                Region().use { region ->
                    region.setRect(IRect(2, 2, 6, 6))
                    canvas.clipRegion(region)
                }
            }
        )
        for (clip in clips) {
            whiteSurface(8, 8).use { surface ->
                clip(surface.canvas)
                Paint().use { paint ->
                    paint.color = Color.RED
                    surface.canvas.drawPaint(paint)
                }
                surface.makeImageSnapshot().use { snapshot ->
                    Bitmap.makeFromImage(snapshot).use { pixels ->
                        assertEquals(Color.RED, pixels.getColor(4, 4))
                        assertEquals(Color.WHITE, pixels.getColor(1, 4))
                        assertEquals(Color.WHITE, pixels.getColor(6, 4))
                    }
                }
            }
        }
    }

    @Test
    fun concatMatricesTransformDrawing() {
        whiteSurface(8, 8).use { surface ->
            surface.canvas.concat(Matrix33.makeTranslate(2f, 3f))
            surface.canvas.drawBlackPixel(0, 0)
            surface.assertSingleBlackPixelAt(2, 3)
        }
        whiteSurface(8, 8).use { surface ->
            surface.canvas.concat(Matrix44(
                1f, 0f, 0f, 3f,
                0f, 1f, 0f, 2f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
            ))
            surface.canvas.drawBlackPixel(0, 0)
            surface.assertSingleBlackPixelAt(3, 2)
        }
    }

    @Test
    fun drawLineAndPoint() {
        whiteSurface(8, 8).use { surface ->
            Paint().use { paint ->
                paint.color = Color.BLACK
                paint.isAntiAlias = false
                paint.strokeWidth = 1f
                surface.canvas.drawLine(1f, 1.5f, 5f, 1.5f, paint)
                surface.canvas.drawPoint(3.5f, 4.5f, paint)
            }
            surface.makeImageSnapshot().use { snapshot ->
                Bitmap.makeFromImage(snapshot).use { pixels ->
                    assertEquals(Color.BLACK, pixels.getColor(2, 1))
                    assertEquals(Color.BLACK, pixels.getColor(3, 4))
                    assertEquals(Color.WHITE, pixels.getColor(3, 3))
                }
            }
        }
    }

    @Test
    fun drawFilledCurvesAndRoundedRectangles() {
        val draws: List<(Canvas, Paint) -> Unit> = listOf(
            { canvas, paint -> canvas.drawArc(1f, 1f, 7f, 7f, 0f, 90f, true, paint) },
            { canvas, paint -> canvas.drawOval(Rect(1f, 1f, 7f, 7f), paint) },
            { canvas, paint -> canvas.drawRRect(RRect.makeLTRB(1f, 1f, 7f, 7f, 2f), paint) }
        )
        for (draw in draws) {
            whiteSurface(8, 8).use { surface ->
                Paint().use { paint ->
                    paint.color = Color.RED
                    paint.isAntiAlias = false
                    draw(surface.canvas, paint)
                }
                surface.makeImageSnapshot().use { snapshot ->
                    Bitmap.makeFromImage(snapshot).use { pixels ->
                        assertEquals(Color.RED, pixels.getColor(5, 5))
                        assertEquals(Color.WHITE, pixels.getColor(0, 0))
                    }
                }
            }
        }
        whiteSurface(8, 8).use { surface ->
            Paint().use { paint ->
                paint.color = Color.RED
                surface.canvas.drawDRRect(
                    RRect.makeLTRB(0f, 0f, 8f, 8f, 1f),
                    RRect.makeLTRB(2f, 2f, 6f, 6f, 1f),
                    paint
                )
            }
            surface.makeImageSnapshot().use { snapshot ->
                Bitmap.makeFromImage(snapshot).use { pixels ->
                    assertEquals(Color.RED, pixels.getColor(1, 4))
                    assertEquals(Color.WHITE, pixels.getColor(4, 4))
                }
            }
        }
    }

    @Test
    fun imageRectAndNinePatchPreserveSourceColors() {
        imageFromIntArray(IntArray(9) { if (it == 4) Color.BLUE else Color.RED }, 3).use { image ->
            whiteSurface(5, 5).use { surface ->
                surface.canvas.drawImageRect(image, Rect(1f, 1f, 2f, 2f), Rect(0f, 0f, 5f, 5f))
                surface.assertPixelsMatch(IntArray(25) { Color.BLUE })
            }
            whiteSurface(5, 5).use { surface ->
                surface.canvas.drawImageNine(image, IRect(1, 1, 2, 2), Rect(0f, 0f, 5f, 5f), FilterMode.NEAREST, null)
                surface.assertPixelsMatch(IntArray(25) { index ->
                    if (index / 5 in 1..3 && index % 5 in 1..3) Color.BLUE else Color.RED
                })
            }
        }
    }

    @Test
    fun layersRestoreSaveCountAndCompositeDrawing() {
        for (bounded in listOf(false, true)) {
            whiteSurface(4, 4).use { surface ->
                val canvas = surface.canvas
                val initialCount = canvas.saveCount
                val saved = if (bounded) {
                    canvas.saveLayer(0f, 0f, 4f, 4f, null)
                } else {
                    canvas.saveLayer(null, null)
                }
                assertEquals(initialCount, saved)
                assertEquals(initialCount + 1, canvas.saveCount)
                Paint().use { paint ->
                    paint.color = Color.RED
                    canvas.drawPaint(paint)
                }
                canvas.save()
                canvas.translate(2f, 2f)
                canvas.restoreToCount(saved)
                assertEquals(initialCount, canvas.saveCount)
                assertContentEquals(Matrix44.IDENTITY.mat, canvas.localToDevice.mat)
                surface.assertPixelsMatch(IntArray(16) { Color.RED })
            }
        }
    }

    @Test
    fun canvasPixelTransferUsesOffsets() {
        whiteSurface(4, 4).use { surface ->
            imageFromIntArray(IntArray(4) { Color.BLUE }, 2).use { image ->
                Bitmap.makeFromImage(image).use { source ->
                    assertTrue(surface.canvas.writePixels(source, 1, 1))
                }
            }
            Bitmap().use { destination ->
                destination.allocPixels(ImageInfo.makeN32Premul(2, 2))
                assertTrue(surface.canvas.readPixels(destination, 1, 1))
                for (y in 0..1) {
                    for (x in 0..1) assertEquals(Color.BLUE, destination.getColor(x, y))
                }
                assertTrue(!surface.canvas.readPixels(destination, 8, 8))
            }
            surface.assertPixelsMatch(IntArray(16) { index ->
                if (index / 4 in 1..2 && index % 4 in 1..2) Color.BLUE else Color.WHITE
            })
        }
    }

    @Test
    fun drawTextBlobProducesInk() = runTest {
        fontInter36().use { font ->
            val blob = assertNotNull(TextBlob.makeFromPosH(font.getStringGlyphs("H"), floatArrayOf(0f), 0f, font))
            blob.use {
                whiteSurface(64, 64).use { surface ->
                    Paint().use { paint ->
                        paint.color = Color.BLACK
                        surface.canvas.drawTextBlob(blob, 8f, 40f, paint)
                    }
                    surface.makeImageSnapshot().use { snapshot ->
                        Bitmap.makeFromImage(snapshot).use { pixels ->
                            assertTrue((0 until 64).any { y -> (0 until 64).any { x -> pixels.getColor(x, y) != Color.WHITE } })
                            assertEquals(Color.BLACK, pixels.getColor(12, 24))
                            assertEquals(Color.WHITE, pixels.getColor(63, 63))
                        }
                    }
                }
            }
        }
    }

    private suspend fun fontInter36() =
        Typeface.makeFromResource("./fonts/Inter-Hinted-Regular.ttf").use { typeface ->
            Font(typeface, 36f)
        }

    @Test
    fun drawString() = runTest {
        val surface = Surface.makeRasterN32Premul(100, 100)

        val bytes = Bitmap.makeFromImage(surface.makeImageSnapshot()).readPixels()!!
        assertTrue {
            bytes.isNotEmpty() && bytes.all { it == 0.toByte() }
        }

        surface.canvas.drawString(
            s = "Hello world!",
            x = 10f, y = 10f,
            font = fontInter36(),
            paint = Paint().apply {
                color = Color.RED
                setStroke(false)
            }
        )

        val bytes2 = Bitmap.makeFromImage(surface.makeImageSnapshot()).readPixels()!!
        assertTrue {
            bytes2.isNotEmpty() && bytes2.any { it != 0.toByte() }
        }
    }

    @Test
    fun testLocalToDevice() = runTest {
        val surface = Surface.makeRasterN32Premul(100, 100)

        val expectedArray = FloatArray(16) { if (it % 5 == 0) 1f else 0f }
        assertContentEquals(expectedArray, surface.canvas.localToDevice.mat)

        surface.canvas.scale(2f, 2f)

        val expectedAfterScale = floatArrayOf(
            2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f
        )
        assertContentEquals(expectedAfterScale, surface.canvas.localToDevice.mat)

        surface.canvas.resetMatrix()
        assertContentEquals(expectedArray, surface.canvas.localToDevice.mat)
    }

    @Test
    fun testSetMatrix() = runTest {
        val surface = Surface.makeRasterN32Premul(100, 100)

        val expectedArray = FloatArray(16) { if (it % 5 == 0) 1f else 0f }
        assertContentEquals(expectedArray, surface.canvas.localToDevice.mat)

        surface.canvas.setMatrix(Matrix33.makeScale(2f, 2f))

        val expectedAfterScale = floatArrayOf(
            2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f
        )

        assertContentEquals(expectedAfterScale, surface.canvas.localToDevice.mat)
    }

    @Test
    fun drawPatch() = runTest {
        // source: https://fiddle.skia.org/c/e96c5f9aa21fb97c25058d9e7b9be3a9

        val surface = Surface.makeRasterN32Premul(8, 8)

        val points = arrayOf(
            3, 1, 4, 2, 5, 1, 7, 3, 6, 4, 7, 5, 5, 7, 4, 6, 3, 7, 1, 5, 2, 4, 1, 3
        ).toList().chunked(2) {
            Point(it[0].toFloat(), it[1].toFloat())
        }

        surface.canvas.drawPatch(
            cubics = points.toTypedArray(),
            colors = intArrayOf(Color.RED, Color.BLUE, Color.YELLOW, Color.CYAN),
            texCoords = null,
            blendMode = BlendMode.DST_OVER,
            paint = Paint()
        )

        val expected = imageFromIntArray(
            pixArray = intArrayOf(
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0xffcb0634.toInt(), 0x00000000, 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0xffb63b49.toInt(), 0xff9b3064.toInt(), 0xff762b8a.toInt(), 0xff4b1cb4.toInt(), 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0xff8d6e73.toInt(), 0xff866479.toInt(), 0xff7a6386.toInt(), 0xff675698.toInt(), 0xff3632c9.toInt(), 0x00000000,
                0x00000000, 0xff36cdc9.toInt(), 0xff67a998.toInt(), 0xff7a9c86.toInt(), 0xff869b79.toInt(), 0xff8d9173.toInt(), 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0xff4be3b4.toInt(), 0xff76d48a.toInt(), 0xff9bcf64.toInt(), 0xffb6c449.toInt(), 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0xffcbf934.toInt(), 0x00000000, 0x00000000, 0x00000000,
                0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,

                ),
            imageWidth = 8
        )

        assertContentSame(expected = expected, got = surface.makeImageSnapshot(), sensitivity = 0.25)
    }

    @Test
    fun testLocalClipBounds() = runTest {
        val surface = Surface.makeRasterN32Premul(8, 8)
        surface.canvas.scale(2f, 2f)
        surface.canvas.clipRect(1f, 1f, 3f, 3f, true)

        assertEquals(Rect(0.5f, 0.5f, 3.5f, 3.5f), surface.canvas.localClipBounds)
    }

    @Test
    fun testEmptyLocalClipBounds() = runTest {
        val surface = Surface.makeRasterN32Premul(4, 4)
        surface.canvas.clipRect(8f, 8f, 12f, 12f, true)

        assertEquals(Rect(0f, 0f, 0f, 0f), surface.canvas.localClipBounds)
    }

    @Test
    fun testTranslate() {
        val surface = whiteSurface(4, 4)
        surface.canvas.translate(2f, 2f)
        surface.canvas.drawBlackPixel(0, 0)

        surface.assertSingleBlackPixelAt(2, 2)
    }


    @Test
    fun testScale() {
        val surface = whiteSurface(4, 4)
        surface.canvas.scale(2f, 2f)
        surface.canvas.drawBlackPixel(1, 1)

        surface.assertPixelsMatch(
            IntArray(16) { index ->
                when (index) {
                    10, 11, 14, 15 -> 0xff000000.toInt()
                    else -> 0xffffffff.toInt()
                }
            }
        )
    }


    @Test
    fun testRotate() {
        val surface = whiteSurface(4, 4)
        surface.canvas.rotate(deg = 90f)
        surface.canvas.drawBlackPixel(3, -1)

        surface.assertSingleBlackPixelAt(0, 3)
    }


    @Test
    fun testRotateXY() {
        val surface = whiteSurface(4, 4)
        surface.canvas.rotate(deg = 90f, x = 2f, y = 2f)
        surface.canvas.drawBlackPixel(0, 0)

        surface.assertSingleBlackPixelAt(3, 0)
    }


    @Test
    fun testSkew() {
        val surface = whiteSurface(4, 4)
        surface.canvas.skew(sx = 0.5f, sy = 0f)
        surface.canvas.drawBlackPixel(0, 2)

        surface.assertPixelsMatch(
            IntArray(16) { index ->
                when (index) {
                    // Skewing skews the shape of the pixel itself, so it becomes a parallelogram
                    9 -> 0xff3f3f3f.toInt()
                    10 -> 0xffbfbfbf.toInt()
                    else -> 0xffffffff.toInt()
                }
            }
        )
    }

    @Test
    fun testSaveLayerRecRect() {
        val surface = whiteSurface(5, 5)

        surface.canvas.saveLayer(
            Canvas.SaveLayerRec(
                bounds = Rect(1f, 1f, 4f, 4f),
                saveLayerFlags = Canvas.SaveLayerFlags(Canvas.SaveLayerFlagsSet.InitWithPrevious)
            )
        )

        val black = Paint().also { it.setARGB(255, 0, 0, 0) }
        surface.canvas.drawRect(Rect(1f, 1f, 4f, 4f), black)

        surface.canvas.restore()

        surface.assertPixelsMatch(
            intArrayOf(
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE,
                Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE,
                Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE,
                Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE,
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE,
            )
        )
    }


    @Test
    fun testSaveLayerRec() {
        val surface = whiteSurface(5, 5)

        surface.canvas.saveLayer(Canvas.SaveLayerRec(saveLayerFlags = Canvas.SaveLayerFlags(Canvas.SaveLayerFlagsSet.InitWithPrevious)))

        val black = Paint().also { it.setARGB(255, 0, 0, 0) }
        surface.canvas.drawRect(Rect(1f, 1f, 4f, 4f), black)

        surface.canvas.restore()

        surface.assertPixelsMatch(
            intArrayOf(
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE,
                Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE,
                Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE,
                Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE,
                Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE,
            )
        )
    }


    @Test
    fun drawAnnotationIsRecordedIntoAPicture() {
        val key = "skiko.test.annotation"
        val value = "payload".encodeToByteArray()

        val recorder = PictureRecorder()
        recorder.beginRecording(Rect(0f, 0f, 8f, 8f))
            .drawAnnotation(Rect(1f, 2f, 3f, 4f), key, Data.makeFromBytes(value))
        val picture = recorder.finishRecordingAsPicture()

        assertEquals(1, picture.approximateOpCount)

        val serialized = picture.serializeToData().bytes
        assertTrue(serialized.containsSequence(key.encodeToByteArray()), "the key did not reach the picture")
        assertTrue(serialized.containsSequence(value), "the value did not reach the picture")
    }

    @Test
    fun drawAnnotationDrawsNothing() {
        val key = "skiko.test.annotation"
        val white = IntArray(16) { Color.WHITE }

        val surface = whiteSurface(4, 4)
        surface.canvas.drawAnnotation(Rect(0f, 0f, 4f, 4f), key)
        surface.assertPixelsMatch(white)

        // An annotation recorded without a value is equally inert when the picture is played back.
        val recorder = PictureRecorder()
        recorder.beginRecording(Rect(0f, 0f, 4f, 4f)).drawAnnotation(Rect(0f, 0f, 4f, 4f), key)
        surface.canvas.drawPicture(recorder.finishRecordingAsPicture())
        surface.assertPixelsMatch(white)
    }


    private fun ByteArray.containsSequence(other: ByteArray): Boolean =
        (0..size - other.size).any { start -> other.indices.all { this[start + it] == other[it] } }


    private fun whiteSurface(width: Int, height: Int): Surface {
        val surface = Surface.makeRasterN32Premul(width, height)
        Paint().use { white ->
            white.color = Color.WHITE
            surface.canvas.drawRect(Rect(0f, 0f, width.toFloat(), height.toFloat()), white)
        }
        return surface
    }


    private fun Canvas.drawBlackPixel(x: Int, y: Int) {
        Paint().use { black ->
            black.color = Color.BLACK
            drawRect(Rect(x.toFloat(), y.toFloat(), (x + 1).toFloat(), (y + 1).toFloat()), black)
        }
    }


    private fun Surface.assertPixelsMatch(pixArray: IntArray) {
        imageFromIntArray(
            pixArray = pixArray,
            imageWidth = width
        ).use { expected ->
            makeImageSnapshot().use { got ->
                assertContentSame(
                    expected = expected,
                    got = got,
                    sensitivity = 0.0
                )
            }
        }
    }


    private fun Surface.assertSingleBlackPixelAt(x: Int, y: Int) {
        val pixArray = IntArray(width * height) { 0xffffffff.toInt() }
        pixArray[y * width + x] = 0xff000000.toInt()

        assertPixelsMatch(pixArray)
    }
}
