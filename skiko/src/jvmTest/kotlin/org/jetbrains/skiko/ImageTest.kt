package org.jetbrains.skiko

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Path
import org.jetbrains.skia.Surface
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import java.nio.file.Path as FilePath

class ImageTest  {
    @Test
    fun imageTest() {
        Surface.makeRasterN32Premul(100, 100).use { surface ->
                val paint = Paint()
                paint.color = -0x10000
                Path().moveTo(20f, 80f).lineTo(50f, 20f).lineTo(80f, 80f).closePath().use { path ->
                    val canvas = surface.canvas
                    canvas.drawPath(path, paint)
                    surface.makeImageSnapshot().use { image ->
                        File("build/tests/ImageTest/").mkdirs()
                        Files.write(
                            FilePath.of("build/tests/ImageTest/polygon_default.png"),
                            image.encodeToData()?.bytes!!
                        )
                        Files.write(
                            FilePath.of("build/tests/ImageTest/polygon_jpeg_default.jpeg"),
                            image.encodeToData(EncodedImageFormat.JPEG)?.bytes!!
                        )
                        Files.write(
                            FilePath.of("build/tests/ImageTest/polygon_jpeg_50.jpeg"),
                            image.encodeToData(EncodedImageFormat.JPEG, 50)?.bytes!!
                        )
                        Files.write(
                            FilePath.of("build/tests/ImageTest/polygon_webp_default.webp"),
                            image.encodeToData(EncodedImageFormat.WEBP)?.bytes!!
                        )
                        Files.write(
                            FilePath.of("build/tests/ImageTest/polygon_webp_50.webp"),
                            image.encodeToData(EncodedImageFormat.WEBP, 50)?.bytes!!
                        )
                    }
            }
        }
    }
}