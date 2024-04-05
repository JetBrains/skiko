package org.jetbrains.skiko.sample
import org.jetbrains.skia.*
import kotlin.math.ceil
import kotlin.math.log2

class ImageInterpolationExample {
    val images: List<Image>

    init {
        val colors = intArrayOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.WHITE,
            Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.BLACK,
            Color.RED, Color.GREEN, Color.BLUE, Color.WHITE,
            Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.BLACK
        )

        val byteArray = ByteArray(colors.size * 4) {
            val colorIndex = it / 4
            val color = colors[colorIndex]
            val channelIndex = it % 4

            when (channelIndex) {
                0 -> Color.getB(color).toByte()
                1 -> Color.getG(color).toByte()
                2 -> Color.getR(color).toByte()
                3 -> Color.getA(color).toByte()
                else -> 0x00.toByte()
            }
        }

        images = listOf(
            SamplingMode.DEFAULT,
            SamplingMode.LINEAR,
            SamplingMode.MITCHELL,
            SamplingMode.CATMULL_ROM
        ).map {
            val image = Image.makeRaster(
                ImageInfo.makeS32(4, 4, ColorAlphaType.PREMUL),
                byteArray,
                16
            )

            val targetWidth = 800
            val targetHeight = 600
            val upscaledBitmap = Bitmap()
            upscaledBitmap.allocN32Pixels(targetWidth, targetHeight)


            upscaledBitmap.peekPixels()?.let { pixels ->
                image.scalePixels(pixels, it, cache = false)
            }

            Image.makeFromBitmap(upscaledBitmap)
        }
    }

    fun draw(canvas: Canvas, rect: Rect) {
        val gridSize = ceil(log2(images.size.toDouble())).toInt()

        for (i in 0 until images.size) {
            val row = i / gridSize
            val col = i % gridSize
            val image = images[i]

            val x = rect.left + col * rect.width / gridSize
            val y = rect.top + row * rect.height / gridSize
            val width = rect.width / gridSize
            val height = rect.height / gridSize

            canvas.drawImageRect(image, Rect.makeXYWH(x, y, width, height))
        }
    }
}