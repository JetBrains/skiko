package org.jetbrains.skiko.sample

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
import org.jetbrains.skiko.FPSCounter
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.currentSystemTheme
import org.jetbrains.skiko.hostOs
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

abstract class Clocks(private val renderApi: GraphicsApi): SkikoRenderDelegate {
    private val withFps = true
    private val fpsCounter = FPSCounter()
    private val platformYOffset = if (hostOs == OS.Ios) 50f else 5f
    private var frame = 0

    var xpos = 0.0
    var ypos = 0.0
    var xOffset = 0.0
    var yOffset = 0.0
    var scale = 1.0
    var rotate = 0.0
//        .setDefaultFontManager(FontMgr.default)
//        .setEnableFallback(false)

    private val style = ParagraphStyle()

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        if (withFps) fpsCounter.tick()
        canvas.translate(xOffset.toFloat(), yOffset.toFloat())
        canvas.scale(scale.toFloat(), scale.toFloat())
        canvas.rotate(rotate.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())

        style.textStyle = TextStyle().apply {
//            this.fontFamilies = arrayOf("nssc", "Noto Color Emoji", "emoji")
//            this.fontFamilies = arrayOf("nssc")
            this.fontSize = 16.0f
        }.setColor(0xFF000000.toInt())

        val renderInfo = ParagraphBuilder(style, fontCollection)
//            .pushStyle(TextStyle().apply {
//                this.fontFamilies = arrayOf("nssc", "Noto Color Emoji", "emoji")
//                this.fontSize = 32.0f
//            }.setColor(0xFF000000.toInt()))
            .addText("\uD83D\uDCE1 Antenna - 天线\n")
            .addText("⁉\uFE0F 〰\uFE0F ⁉\uFE0F\n")
            .addText("\uD83D\uDE32 \uD83E\uDD14 \uD83D\uDCA1\n")
            .addText("\uD83C\uDF0D Earth - 地球\n")
            .addText("\uD83D\uDE80 Space rocket - 太空火箭\n")
            .addText("\uD83C\uDF14 Moon - 月亮\n")
            .addText("\uD83D\uDCAB Stars - 星星\n")
            .addText("\uD83C\uDF0C Galaxy - 星系\n")
            .addText("\uD83D\uDEF8 UFO - 飞碟\n")
            .addText("\uD83D\uDC7D Alien - 外星人\n")
            .addText("\uD83D\uDC4B \uD83E\uDD1D \uD83C\uDF7B \n")

//            .popStyle()
            .build()
        renderInfo.layout(Float.POSITIVE_INFINITY)
        renderInfo.paint(canvas, 5f, platformYOffset)

//        println("Reset\n")
        canvas.resetMatrix()
    }

    companion object {
        val fontCollection = FontCollection()
//            .setDefaultFontManager(FontMgr.wrapper)
//            .setEnableFallback(false)
    }
}
