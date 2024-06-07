package org.jetbrains.skiko.sample

import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import org.w3c.dom.HTMLCanvasElement


fun main() {
    //runClocksApp()
    runEmojiStoryApp()
}

internal fun runClocksApp() {
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    canvas.setAttribute("tabindex", "0")
    val skiaLayer = SkiaLayer()
    val clocks = WebClocks(skiaLayer, canvas)
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, clocks)
    skiaLayer.needRedraw()
}

private val notoColorEmoji = "https://storage.googleapis.com/skia-cdn/misc/NotoColorEmoji.ttf"
private val notoSancSC = "./NotoSansSC-Regular.ttf"

internal fun runEmojiStoryApp() {
    val canvas = document.getElementById("SkikoTarget") as HTMLCanvasElement
    canvas.setAttribute("tabindex", "0")
    val skiaLayer = SkiaLayer()
    val app = EmojiStory()
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, app)
    skiaLayer.attachTo(canvas)

    MainScope().launch {
        val notoEmojisBytes = loadRes(notoColorEmoji).toByteArray()
        val notoSansSCBytes = loadRes(notoSancSC).toByteArray()
        val notoEmojiTypeface = Typeface.makeFromData(Data.makeFromBytes(notoEmojisBytes))
        val notoSansSCTypeface = Typeface.makeFromData(Data.makeFromBytes(notoSansSCBytes))

        val tfp = TypefaceFontProvider.createAsFallbackProvider().apply {
            registerTypeface(notoEmojiTypeface)
            registerTypeface(notoSansSCTypeface)
        }
        EmojiStory.fontCollection.setDefaultFontManager(FontMgr.defaultWithFallbackFontProvider(tfp))

        skiaLayer.needRedraw()
    }
}
