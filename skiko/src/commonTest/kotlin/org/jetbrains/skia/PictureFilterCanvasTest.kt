package org.jetbrains.skia

import kotlin.test.Test
import kotlin.test.assertEquals

class PictureFilterCanvasTest {
    @Test
    fun canOverrideOnDrawPicture() {
        val surface = Surface.makeRasterN32Premul(16, 16)
        val bounds = Rect(0f, 0f, 16f, 16f)
        var callCount = 0

        // Prepare picture
        val recorder = PictureRecorder()
        val recordingCanvas = recorder.beginRecording(bounds)
        val placeholder = Picture.makePlaceholder(bounds)
        recordingCanvas.drawPicture(placeholder)
        val picture = recorder.finishRecordingAsPicture()

        // Filtering
        val filterCanvas = object : PictureFilterCanvas(surface.canvas) {
            override fun onDrawPicture(picture: Picture, matrix: Matrix33?, paint: Paint?): Boolean {
                callCount++
                drawRect(
                    Rect(0f, 0f, 8f, 8f),
                    Paint().apply { color = Color.RED }
                )
                return true
            }
        }
        filterCanvas.drawPicture(picture)
        filterCanvas.close()

        // Render result
        val pixels = Bitmap.makeFromImage(surface.makeImageSnapshot())

        assertEquals(1, callCount)
        assertEquals(Color.RED, pixels.getColor(2, 2))

        placeholder.close()
        picture.close()
        recorder.close()
        surface.close()
    }
}