package org.jetbrains.skia

import org.jetbrains.skia.tests.assertCloseEnough
import org.jetbrains.skiko.tests.SkipWasmTarget
import kotlin.test.Test
import kotlin.test.assertEquals

class PictureTest {
    @Test
    fun canMakeShader() {
        val pic = Picture.makePlaceholder(Rect(0.0f, 0.0f, 32.0f, 32.0f))
        val localMatrix = Matrix33(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f)
        val tile = Rect(0.0f, 0.0f, 16.0f, 16.0f)
        pic.makeShader(FilterTileMode.MIRROR, FilterTileMode.MIRROR, FilterMode.LINEAR)
        pic.makeShader(FilterTileMode.MIRROR, FilterTileMode.MIRROR, FilterMode.LINEAR, localMatrix)
        pic.makeShader(FilterTileMode.MIRROR, FilterTileMode.MIRROR, FilterMode.LINEAR, localMatrix, tile)
    }

    @Test
    fun canGetCullRect() {
        val size = Rect(0.0f, 0.0f, 32.0f, 32.0f)
        val pic = Picture.makePlaceholder(size)
        val cullRect = pic.cullRect
        assertCloseEnough(size, cullRect)
    }


    @Test
    fun canReplay() {
        val size = Rect(0.0f, 0.0f, 32.0f, 32.0f)
        val recorder = PictureRecorder()
        val canvas = recorder.beginRecording(size)
        canvas.drawRect(Rect(10.0f, 10.0f, 20.0f, 20.0f), Paint().apply { color = Color.RED })
        val pic = recorder.finishRecordingAsPicture()

        val surface = Surface.makeRasterN32Premul(32, 32)
        pic.playback(surface.canvas)
        assertEquals(Color.RED, Bitmap.makeFromImage(surface.makeImageSnapshot()).getColor(15, 15))
    }


    @Test
    fun canReplayWithCallback() {
        val size = Rect(0.0f, 0.0f, 32.0f, 32.0f)
        val recorder = PictureRecorder()
        val canvas = recorder.beginRecording(size)
        canvas.drawRect(Rect(10.0f, 10.0f, 20.0f, 20.0f), Paint().apply { color = Color.RED })
        canvas.drawRect(Rect(12.0f, 12.0f, 18.0f, 18.0f), Paint().apply { color = Color.WHITE })
        val pic = recorder.finishRecordingAsPicture()

        val surface = Surface.makeRasterN32Premul(32, 32)
        var drawCount = 0
        pic.playback(surface.canvas) { drawCount += 1; drawCount > 1 } // Draw only once
        assertEquals(2, drawCount)
        assertEquals(Color.RED, Bitmap.makeFromImage(surface.makeImageSnapshot()).getColor(15, 15))
    }
}
