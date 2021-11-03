package org.jetbrains.skiko.util

import org.jetbrains.skia.Image
import org.jetbrains.skia.util.imageFromIntArray

@Suppress("RemoveRedundantCallsOfConversionMethods")
val PIXELS_COLORS_8X8  by lazy { intArrayOf(
    0xffff0000.toInt(), 0xffff0000.toInt(), 0xff00ff00.toInt(), 0xff00ff00.toInt(), 0xff0000ff.toInt(), 0xff0000ff.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
    0xffff0000.toInt(), 0xffff0000.toInt(), 0xff00ff00.toInt(), 0xff00ff00.toInt(), 0xff0000ff.toInt(), 0xff0000ff.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
    0xff00ffff.toInt(), 0xff00ffff.toInt(), 0xffff00ff.toInt(), 0xffff00ff.toInt(), 0xffffff00.toInt(), 0xffffff00.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
    0xff00ffff.toInt(), 0xff00ffff.toInt(), 0xffff00ff.toInt(), 0xffff00ff.toInt(), 0xffffff00.toInt(), 0xffffff00.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
    0xffffffff.toInt(), 0xffffffff.toInt(), 0xff000000.toInt(), 0xff000000.toInt(), 0x80000000.toInt(), 0x80000000.toInt(), 0x03000000.toInt(), 0x00000000.toInt(),
    0xffffffff.toInt(), 0xffffffff.toInt(), 0xff000000.toInt(), 0xff000000.toInt(), 0x80000000.toInt(), 0x80000000.toInt(), 0x03000000.toInt(), 0x00000000.toInt(),
    0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
    0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(), 0x00000000.toInt(),
) }

val IMAGE_COLORS_8X8 by lazy { imageFromIntArray(PIXELS_COLORS_8X8, 8) }

fun Image.Companion.makeSolidColor(color: Int, width: Int, height: Int)
    = imageFromIntArray(IntArray(width * height) { color }, width)