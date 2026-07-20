package org.jetbrains.skiko.node

import org.jetbrains.skia.*
import org.jetbrains.skia.tests.assertCloseEnough
import kotlin.test.Test
import kotlin.test.assertContentEquals
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
    fun drawingANodeDoesNotReferenceTheDrawingNodeBack() {
        val context = RenderNodeContext()
        val parent = RenderNode(context)
        val child = RenderNode(context)

        child.bounds = Rect(0f, 0f, 10f, 10f)
        child.beginRecording().drawRect(0f, 0f, 10f, 10f, Paint())
        child.endRecording()

        parent.bounds = Rect(0f, 0f, 10f, 10f)
        child.drawInto(parent.beginRecording())
        parent.endRecording()

        // The parent holds the child alive, through both its recorded content and
        // its dependencies set. The child must not hold the parent back: a cycle
        // would leak both for the lifetime of the process.
        assertEquals(1, parent.refCount)
        assertTrue(child.refCount > 1)

        // Releasing the parent releases everything it held on the child.
        parent.close()
        assertEquals(1, child.refCount)

        child.close()
        context.close()
    }

    @Test
    fun mutatedTreeRendersLikeAFreshOne() {
        // A node keeps its snapshot across property changes and rebuilds it when the
        // content it inlined changes. Either way the result has to match a tree built
        // with those values from the start.
        fun render(mutate: Boolean): ByteArray {
            val context = RenderNodeContext()
            val parent = RenderNode(context)
            val child = RenderNode(context)
            val surface = Surface.makeRasterN32Premul(16, 16)

            child.bounds = Rect(0f, 0f, 8f, 8f)
            child.beginRecording().drawRect(0f, 0f, 8f, 8f, Paint().apply { color = Color.RED })
            child.endRecording()
            parent.bounds = Rect(0f, 0f, 16f, 16f)
            val parentCanvas = parent.beginRecording()
            parentCanvas.drawRect(0f, 0f, 16f, 16f, Paint().apply { color = Color.GREEN })
            child.drawInto(parentCanvas)
            parent.endRecording()

            if (mutate) {
                // Draw once so both nodes snapshot, then move the child and re-record it.
                parent.drawInto(surface.canvas)
                surface.canvas.clear(Color.TRANSPARENT)
            }
            child.translationX = 4f
            child.beginRecording().drawRect(0f, 0f, 8f, 8f, Paint().apply { color = Color.BLUE })
            child.endRecording()
            parent.drawInto(surface.canvas)

            val bytes = Bitmap.makeFromImage(surface.makeImageSnapshot()).readPixels()!!
            surface.close(); parent.close(); child.close(); context.close()
            return bytes
        }

        assertContentEquals(render(mutate = false), render(mutate = true))
    }

    @Test
    fun nodeRecordedIntoPictureBeforeItHasDrawn() {
        // Recording the node into a picture replays it, which builds the snapshot on the
        // way. That recording must not go through the recorder the replay is using.
        val context = RenderNodeContext()
        val node = RenderNode(context)
        node.bounds = Rect(0f, 0f, 100f, 100f)
        node.beginRecording().drawRect(Rect(20f, 20f, 40f, 40f), Paint().apply { color = Color.RED })
        node.endRecording()

        val recorder = PictureRecorder()
        node.drawInto(recorder.beginRecording(Rect(0f, 0f, 100f, 100f)))
        val picture = recorder.finishRecordingAsPicture()

        val viaPicture = Surface.makeRasterN32Premul(100, 100)
        viaPicture.canvas.drawPicture(picture)
        val direct = Surface.makeRasterN32Premul(100, 100)
        node.drawInto(direct.canvas)

        assertContentEquals(
            Bitmap.makeFromImage(direct.makeImageSnapshot()).readPixels()!!,
            Bitmap.makeFromImage(viaPicture.makeImageSnapshot()).readPixels()!!
        )

        picture.close(); viaPicture.close(); direct.close(); node.close(); context.close()
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
