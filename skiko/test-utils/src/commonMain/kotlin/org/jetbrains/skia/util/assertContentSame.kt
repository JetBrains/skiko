package org.jetbrains.skia.util

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import kotlin.math.abs

fun assertContentSame(expected: Image, got: Image, sensitivity: Double) {
    require(sensitivity in 0.0..1.0)
    val sensitivity255 = (sensitivity * 255).toInt()
    if (expected.width == got.width && expected.height == got.height) {
        val expectedPixels = Bitmap.makeFromImage(expected)
        val gotPixels = Bitmap.makeFromImage(got)
        for (y in 0 until expected.height) {
            for (x in 0 until expected.width) {
                val color1 = expectedPixels.getColor(x, y)
                val color2 = gotPixels.getColor(x, y)

                val pixelsAreSame = run {
                    if (abs(Color.getR(color1) - Color.getR(color2)) > sensitivity255) {
                        return@run false
                    }
                    if (abs(Color.getG(color1) - Color.getG(color2)) > sensitivity255) {
                        return@run false
                    }
                    if (abs(Color.getB(color1) - Color.getB(color2)) > sensitivity255) {
                        return@run false
                    }
                    if (abs(Color.getA(color1) - Color.getA(color2)) > sensitivity255) {
                        return@run false
                    }
                    true
                }

                if (!pixelsAreSame) {
                    throw AssertionError("Image mismatch at pixel [$x, $y]: expected $color1, got $color2")
                }
            }
        }
    } else {
        throw AssertionError("Image size mismatch")
    }
}