package org.jetbrains.skiko.sample.js

import org.jetbrains.skia.Color
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Surface
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle

fun drawParagraphExample(surface: Surface) {
    val fontCollection = FontCollection()
        .setDefaultFontManager(FontMgr.default)

    val p = ParagraphBuilder(ParagraphStyle(), fontCollection)
        .pushStyle(
            TextStyle().apply {
                color = Color.makeRGB(1, 0, 0)
                fontSize = 25f
            }
        )
        .addText("Hello World!\nПривет Мир!")
        .popStyle()
        .build()

    p.layout(Float.POSITIVE_INFINITY)
    p.paint(surface.canvas, 10f, 10f)
    surface.flushAndSubmit()
}