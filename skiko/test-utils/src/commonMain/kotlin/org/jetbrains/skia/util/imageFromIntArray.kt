package org.jetbrains.skia.util

import org.jetbrains.skia.*

fun makeByteArrayFromRGBArray(pixArray: IntArray): ByteArray {
    var result = ByteArray(pixArray.size * 4)
    var off = 0
    for (pix in pixArray) {
        result[off++] = Color.getR(pix).toByte()
        result[off++] = Color.getG(pix).toByte()
        result[off++] = Color.getB(pix).toByte()
        result[off++] = Color.getA(pix).toByte()
    }

    return result
}

fun imageFromIntArray(pixArray: IntArray, imageWidth: Int) = Image.makeRaster(
    imageInfo = ImageInfo(imageWidth, pixArray.size / imageWidth, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL),
    rowBytes = imageWidth * 4 /* Four bytes per pixel */,
    bytes = makeByteArrayFromRGBArray(pixArray)
)