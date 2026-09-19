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
        val context = RenderNodeContext(snapshotCache = true)
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
        fun rendered(mutate: Boolean) = render { canvas ->
            val child = node(Rect(0f, 0f, 8f, 8f))
            fill(child, Color.RED, 8f, 8f)
            val parent = node(Rect(0f, 0f, 16f, 16f))
            val parentCanvas = parent.beginRecording()
            parentCanvas.drawRect(0f, 0f, 16f, 16f, Paint().apply { color = Color.GREEN })
            child.drawInto(parentCanvas)
            parent.endRecording()

            if (mutate) {
                // Draw once so both nodes snapshot, then move the child and re-record it.
                parent.drawInto(canvas)
                canvas.clear(Color.TRANSPARENT)
            }
            child.translationX = 4f
            fill(child, Color.BLUE, 8f, 8f)
            parent.drawInto(canvas)
        }

        assertContentEquals(rendered(mutate = false), rendered(mutate = true))
    }

    @Test
    fun snapshotCacheRendersTheSameAsDirectDrawing() {
        // Renders a parent/child pair twice, changing only properties that are
        // applied around the recorded content, which the snapshot has to survive.
        fun rendered(snapshotCache: Boolean) = render(snapshotCache = snapshotCache) { canvas ->
            val child = node(Rect(0f, 0f, 8f, 8f))
            fill(child, Color.RED, 8f, 8f)
            val parent = node(Rect(0f, 0f, 16f, 16f))
            val parentCanvas = parent.beginRecording()
            parentCanvas.drawRect(0f, 0f, 16f, 16f, Paint().apply { color = Color.GREEN })
            child.drawInto(parentCanvas)
            parent.endRecording()

            parent.drawInto(canvas)

            child.alpha = 0.5f
            child.translationX = 4f
            parent.alpha = 0.75f
            parent.drawInto(canvas)
        }

        assertContentEquals(rendered(snapshotCache = false), rendered(snapshotCache = true))
    }

    @Test
    fun nodeRecordedIntoPictureBeforeItHasDrawn() {
        // Recording the node into a picture replays it, which builds the snapshot on the
        // way. That recording must not go through the recorder the replay is using.
        val context = RenderNodeContext(snapshotCache = true)
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
    fun nestedContentInvalidatesTheSnapshotsThatInlinedIt() {
        // A node under alpha is unrolled rather than snapshotted, so it holds no snapshot
        // of its own while still being inlined into the one above it. It has to keep
        // relaying changes from below, or that outer snapshot replays stale content.
        fun rendered(middleAlpha: Float, mutate: Boolean) = render { canvas ->
            val inner = node(Rect(0f, 0f, 8f, 8f))
            fill(inner, if (mutate) Color.RED else Color.BLUE, 8f, 8f)
            val middle = node(Rect(0f, 0f, 16f, 16f))
            middle.alpha = middleAlpha
            inner.drawInto(middle.beginRecording())
            middle.endRecording()
            val outer = node(Rect(0f, 0f, 16f, 16f))
            middle.drawInto(outer.beginRecording())
            outer.endRecording()

            if (mutate) {
                // Draw once so the whole chain snapshots, then re-record the innermost node.
                outer.drawInto(canvas)
                canvas.clear(Color.TRANSPARENT)
                fill(inner, Color.BLUE, 8f, 8f)
            }
            outer.drawInto(canvas)
        }

        for (middleAlpha in listOf(1.0f, 0.5f)) {
            assertContentEquals(
                rendered(middleAlpha, mutate = false),
                rendered(middleAlpha, mutate = true),
                "stale content at middleAlpha=$middleAlpha"
            )
        }
    }

    @Test
    fun changesBetweenTwoDrawsReachTheSnapshots() {
        // The observers are notified once per draw, so of several changes made between two
        // draws only the first is relayed. Each sequence below starts from content the
        // snapshot would still be showing if a later change went unnoticed.
        fun rendered(
            before: RenderScope.(RenderNode) -> Unit,
            change: RenderScope.(middle: RenderNode, inner: RenderNode) -> Unit
        ) = render { canvas ->
            val inner = node(Rect(0f, 0f, 8f, 8f))
            before(inner)
            val middle = node(Rect(0f, 0f, 16f, 16f))
            inner.drawInto(middle.beginRecording())
            middle.endRecording()
            val outer = node(Rect(0f, 0f, 16f, 16f))
            middle.drawInto(outer.beginRecording())
            outer.endRecording()

            outer.drawInto(canvas)
            canvas.clear(Color.TRANSPARENT)
            change(middle, inner)
            outer.drawInto(canvas)
        }

        val expected = rendered({ fill(it, Color.BLUE, 8f, 8f) }, { _, _ -> })

        assertContentEquals(expected, rendered({ fill(it, Color.RED, 8f, 8f) }, { _, inner ->
            fill(inner, Color.GREEN, 8f, 8f)
            fill(inner, Color.BLUE, 8f, 8f)
        }), "the second of two re-recordings was not relayed")

        assertContentEquals(expected, rendered({ fill(it, Color.RED, 8f, 8f) }, { middle, inner ->
            inner.drawInto(middle.beginRecording())
            middle.endRecording()
            fill(inner, Color.BLUE, 8f, 8f)
        }), "a change below a re-recorded node was not relayed")

        assertContentEquals(expected, rendered({ it.translationX = 8f; fill(it, Color.BLUE, 8f, 8f) }, { _, inner ->
            fill(inner, Color.BLUE, 8f, 8f)
            inner.translationX = 0f
        }), "a position change after a re-recording was not relayed")
    }

    @Test
    fun aChangeReachesEveryObserverOfTheChangedNode() {
        // The shared node is drawn by both branches, so one notification has to invalidate
        // the snapshots of both.
        fun rendered(mutate: Boolean) = render { canvas ->
            val shared = node(Rect(0f, 0f, 8f, 8f))
            fill(shared, if (mutate) Color.RED else Color.BLUE, 8f, 8f)
            val left = node(Rect(0f, 0f, 8f, 8f))
            shared.drawInto(left.beginRecording())
            left.endRecording()
            val right = node(Rect(8f, 0f, 16f, 8f))
            shared.drawInto(right.beginRecording())
            right.endRecording()
            val outer = node(Rect(0f, 0f, 16f, 16f))
            val outerCanvas = outer.beginRecording()
            left.drawInto(outerCanvas)
            right.drawInto(outerCanvas)
            outer.endRecording()

            if (mutate) {
                outer.drawInto(canvas)
                canvas.clear(Color.TRANSPARENT)
                fill(shared, Color.BLUE, 8f, 8f)
            }
            outer.drawInto(canvas)
        }

        assertContentEquals(rendered(mutate = false), rendered(mutate = true))
    }

    @Test
    fun aShadowAppearingAfterAnotherChangeReachesTheSnapshots() {
        // A shadow is only correct at the transform it was recorded under, so nothing
        // inlining it may be snapshotted. That answer has to reach the nodes above even
        // when they were already told about an earlier change and would otherwise be
        // left alone until the next draw.
        fun rendered(shadowAddedAfterADraw: Boolean) = render(width = 80, height = 80) { canvas ->
            context.setLightingInfo(
                centerX = 40f, centerY = 40f, centerZ = 100f, radius = 200f,
                ambientShadowAlpha = 0.6f, spotShadowAlpha = 0.6f
            )

            val inner = node(Rect(0f, 0f, 20f, 20f))
            inner.setClipRect(Rect(0f, 0f, 20f, 20f))
            if (!shadowAddedAfterADraw) inner.shadowElevation = 8f
            fill(inner, if (shadowAddedAfterADraw) Color.RED else Color.BLUE, 20f, 20f)
            val middle = node(Rect(0f, 0f, 60f, 60f))
            inner.drawInto(middle.beginRecording())
            middle.endRecording()
            val outer = node(Rect(0f, 0f, 60f, 60f))
            middle.drawInto(outer.beginRecording())
            outer.endRecording()

            if (shadowAddedAfterADraw) {
                outer.drawInto(canvas)
                canvas.clear(Color.TRANSPARENT)
                fill(inner, Color.BLUE, 20f, 20f)
                inner.shadowElevation = 8f
            }

            // Replay somewhere other than where the content was recorded: a shadow that
            // was snapshotted keeps the geometry it was recorded with and shows up here.
            canvas.save()
            canvas.translate(20f, 20f)
            outer.drawInto(canvas)
            canvas.restore()
        }

        assertContentEquals(rendered(shadowAddedAfterADraw = false), rendered(shadowAddedAfterADraw = true))
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

    // The nodes a test builds, the surface they draw into and the pixels that come out.
    // Everything created here is closed even when an assertion fails partway through.
    private class RenderScope(val context: RenderNodeContext, val surface: Surface) {
        private val nodes = mutableListOf<RenderNode>()

        fun node(bounds: Rect): RenderNode =
            RenderNode(context).also {
                it.bounds = bounds
                nodes += it
            }

        fun fill(node: RenderNode, color: Int, width: Float, height: Float) {
            node.beginRecording().drawRect(0f, 0f, width, height, Paint().apply { this.color = color })
            node.endRecording()
        }

        fun close() {
            nodes.forEach(RenderNode::close)
            surface.close()
            context.close()
        }
    }

    private fun render(
        width: Int = 16,
        height: Int = 16,
        snapshotCache: Boolean = true,
        body: RenderScope.(canvas: Canvas) -> Unit
    ): ByteArray {
        val scope = RenderScope(
            RenderNodeContext(snapshotCache = snapshotCache),
            Surface.makeRasterN32Premul(width, height)
        )
        try {
            scope.body(scope.surface.canvas)
            return Bitmap.makeFromImage(scope.surface.makeImageSnapshot()).readPixels()!!
        } finally {
            scope.close()
        }
    }
}
