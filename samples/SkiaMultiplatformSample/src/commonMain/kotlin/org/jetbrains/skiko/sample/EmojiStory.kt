package org.jetbrains.skiko.sample

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.hostOs

class EmojiStory : SkikoRenderDelegate {

    private val platformYOffset = if (hostOs == OS.Ios) 50f else 5f

    private val style = ParagraphStyle().apply {
        replaceTabCharacters = true
        textStyle = TextStyle().apply {
            this.fontSize = 16.0f
        }.setColor(0xFF000000.toInt())
    }

    private val paragraph = ParagraphBuilder(style, fontCollection)
        .addText("\uD83D\uDCE1\tAntenna\t-\t天线\t\n")
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
        .build()

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        paragraph.layout(Float.POSITIVE_INFINITY)
        paragraph.paint(canvas, 5f, platformYOffset)
        canvas.resetMatrix()
    }

    companion object {
        val fontCollection = FontCollection()
            .setDefaultFontManager(FontMgr.default)
    }
}