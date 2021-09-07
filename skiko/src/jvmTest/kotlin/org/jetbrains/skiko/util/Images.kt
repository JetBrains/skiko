package org.jetbrains.skiko.util

import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.abs

fun isContentSame(img1: BufferedImage, img2: BufferedImage, sensitivity: Double): Boolean {
    require(sensitivity in 0.0..1.0)
    val sensitivity255 = (sensitivity * 255).toInt()
    if (img1.width == img2.width && img1.height == img2.height) {
        for (x in 0 until img1.width) {
            for (y in 0 until img1.height) {
                val color1 = Color(img1.getRGB(x, y))
                val color2 = Color(img2.getRGB(x, y))
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