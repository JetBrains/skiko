package org.jetbrains.skiko.node

import org.jetbrains.skia.*
import org.jetbrains.skia.tests.assertCloseEnough
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RenderNodeTest {
    @Test
    fun verifyInterop() {
        val surface = Surface.makeRasterN32Premul(16, 16)
        val context = RenderNodeContext()
        val node = RenderNode(context)

        context.setLightingInfo(0f, 0f, 10f, 1f, 1f, 1f)

        node.layerPaint = Paint().apply { color = Color.RED }
        assertTrue(node.layerPaint != null)

        node.bounds = Rect(1f, 2f, 3f, 4f)
        assertCloseEnough(Rect(1f, 2f, 3f, 4f), node.bounds)

        node.pivot = Point(5f, 6f)
        assertCloseEnough(Point(5f, 6f), node.pivot)

        node.alpha = 0.42f
        assertCloseEnough(0.42f, node.alpha)

        node.scaleX = 2f
        assertCloseEnough(2f, node.scaleX)

        node.scaleY = 3f
        assertCloseEnough(3f, node.scaleY)

        node.translationX = 7f
        assertCloseEnough(7f, node.translationX)

        node.translationY = 8f
        assertCloseEnough(8f, node.translationY)

        node.rotationX = 9f
        assertCloseEnough(9f, node.rotationX)

        node.rotationY = 10f
        assertCloseEnough(10f, node.rotationY)

        node.rotationZ = 11f
        assertCloseEnough(11f, node.rotationZ)

        node.shadowElevation = 1f
        assertCloseEnough(1f, node.shadowElevation)

        node.ambientShadowColor = Color.GREEN
        assertEquals(Color.GREEN, node.ambientShadowColor)

        node.spotShadowColor = Color.BLUE
        assertEquals(Color.BLUE, node.spotShadowColor)

        node.cameraDistance = 12f
        assertCloseEnough(12f, node.cameraDistance)

        node.setClipRect(0f, 0f, 16f, 16f)
        node.setClipRRect(0f, 0f, 16f, 16f, floatArrayOf(1f))
        node.setClipPath(Path())
        node.setClipPath(null)

        node.clip = true
        assertTrue(node.clip)

        val recordCanvas = node.beginRecording()
        recordCanvas.drawRect(0f,0f,16f,16f, Paint().apply { color = Color.BLACK })
        node.endRecording()

        node.drawInto(surface.canvas)

        surface.close()
        node.close()
        context.close()
    }

    @Test
    fun pictureCullRect() {
        val context = RenderNodeContext(measureDrawBounds = true)
        val node = RenderNode(context)
        node.bounds = Rect(0f, 0f, 100f, 100f)

        val recordCanvas = node.beginRecording()
        recordCanvas.drawRect(20f,20f,40f,40f, Paint())
        node.endRecording()

        val pictureRecorder = PictureRecorder()
        val bbhFactory = RTreeFactory()
        val pictureCanvas = pictureRecorder.beginRecording(0f, 0f, 100f, 100f, bbhFactory)
        node.drawInto(pictureCanvas)
        val picture = pictureRecorder.finishRecordingAsPicture()

        assertEquals(Rect(20f, 20f, 40f, 40f), picture.cullRect)

        picture.close()
        node.close()
        context.close()
    }
}
