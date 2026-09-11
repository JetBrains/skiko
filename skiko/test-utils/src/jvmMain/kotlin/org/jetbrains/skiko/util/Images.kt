package org.jetbrains.skiko.util

import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.Pixmap
import java.awt.Color
import java.io.InputStream
import kotlin.math.abs

fun isContentSame(img1: Image, img2: Image, sensitivity: Double): Boolean {
    require(sensitivity in 0.0..1.0)
    val sensitivity255 = (sensitivity * 255).toInt()
    if (img1.width == img2.width && img1.height == img2.height) {
        val pixMap1 = Pixmap()
        val pixMap2 = Pixmap()

        pixMap1.reset(
            img1.imageInfo,
            Data.makeUninitialized(img1.bytesPerPixel * img1.width * img1.height),
            img1.bytesPerPixel * img1.width
        )
        pixMap2.reset(
            img2.imageInfo,
            Data.makeUninitialized(img2.bytesPerPixel * img2.width * img2.height),
            img2.bytesPerPixel * img2.width
        )
        check(img1.readPixels(pixMap1, 0, 0, false))
        check(img2.readPixels(pixMap2, 0, 0, false))

        for (y in 0 until img1.height) {
            for (x in 0 until img1.width) {
                val color1 = Color(pixMap1.getColor(x, y))
                val color2 = Color(pixMap2.getColor(x, y))
                if (abs(color1.red - color2.red) > sensitivity255) {
                    return false
                }
                if (abs(color1.green - color2.green) > sensitivity255) {
                    return false
                }
                if (abs(color1.blue - color2.blue) > sensitivity255) {
                    return false
                }
                if (abs(color1.alpha - color2.alpha) > sensitivity255) {
                    return false
                }
            }
        }
    } else {
        return false
    }
    return true
}

fun loadResourceImage(path: String) = useResource(path, ::loadImage)

inline fun <T> useResource(
    resourcePath: String,
    block: (InputStream) -> T
): T = openResource(resourcePath).use(block)

fun openResource(resourcePath: String): InputStream {
    val classLoader = Thread.currentThread().contextClassLoader!!
    return requireNotNull(classLoader.getResourceAsStream(resourcePath)) {
        "Resource $resourcePath not found"
    }
}

fun loadImage(inputStream: InputStream): Image =
    Image.makeFromEncoded(inputStream.readAllBytes())
