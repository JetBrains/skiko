package org.jetbrains.skiko.util

import java.awt.image.BufferedImage

fun isContentSame(img1: BufferedImage, img2: BufferedImage): Boolean {
    if (img1.width == img2.width && img1.height == img2.height) {
        for (x in 0 until img1.width) {
            for (y in 0 until img1.height) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false
                }
            }
        }
    } else {
        return false
    }
    return true
}