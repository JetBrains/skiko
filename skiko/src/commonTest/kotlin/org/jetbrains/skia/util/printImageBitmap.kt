package org.jetbrains.skia.util

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image

fun Image.printBitmap() {
    val pixels = Bitmap.makeFromImage(this)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pix = pixels.getColor(x, y)
            val pixStr = pix.toUInt().toString(16).padStart(8, '0')
            print("0x$pixStr.toInt(), ")
        }
        println()
    }
}