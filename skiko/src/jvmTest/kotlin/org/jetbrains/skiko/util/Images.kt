package org.jetbrains.skiko.util

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ByteBuffer
import org.jetbrains.skia.Image
import java.awt.Color
import java.io.InputStream
import kotlin.math.abs

fun isContentSame(img1: Image, img2: Image, sensitivity: Double): Boolean {
    require(sensitivity in 0.0..1.0)
    val sensitivity255 = (sensitivity * 255).toInt()
    if (img1.width == img2.width && img1.height == img2.height) {
        val pixels1 = Bitmap.makeFromImage(img1).readPixels()!!.toIntArray()
        val pixels2 = Bitmap.makeFromImage(img2).readPixels()!!.toIntArray()
        for (y in 0 until img1.height) {
            for (x in 0 until img1.width) {
                val color1 = Color(pixels1[y * img1.width + x])
                val color2 = Color(pixels2[y * img1.width + x])
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

private fun ByteArray.toIntArray(): IntArray {
    val buf = ByteBuffer.wrap(this).asIntBuffer()
    val array = IntArray(buf.remaining())
    buf.get(array)
    return array
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
