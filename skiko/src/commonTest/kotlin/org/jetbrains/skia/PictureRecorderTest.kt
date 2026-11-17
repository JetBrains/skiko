package org.jetbrains.skia

import org.jetbrains.skia.impl.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PictureRecorderTest {
    @Test
    fun recordingCanvasTracksRecordingAndExplicitCullBounds() {
        PictureRecorder().use { recorder ->
            assertNull(recorder.recordingCanvas)
            val canvas = recorder.beginRecording(Rect(0f, 0f, 16f, 16f))
            val recordingCanvas = assertNotNull(recorder.recordingCanvas)
            assertEquals(canvas, recordingCanvas)
            Paint().use { paint ->
                paint.color = Color.BLUE
                recordingCanvas.drawRect(Rect(2f, 2f, 6f, 6f), paint)
            }
            val cull = Rect(1f, 1f, 8f, 8f)
            recorder.finishRecordingAsPicture(cull).use { picture ->
                assertEquals(cull, picture.cullRect)
                assertNull(recorder.recordingCanvas)
                Surface.makeRasterN32Premul(8, 8).use { surface ->
                    picture.playback(surface.canvas)
                    surface.makeImageSnapshot().use { snapshot ->
                        Bitmap.makeFromImage(snapshot).use { pixels ->
                            assertEquals(Color.BLUE, pixels.getColor(3, 3))
                            assertEquals(Color.TRANSPARENT, pixels.getColor(0, 0))
                        }
                    }
                }
            }
        }
    }
}
