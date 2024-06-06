package org.jetbrains.skiko.sample

import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.makeFromFile
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import java.awt.Dimension
import java.nio.file.Paths
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

fun resourcePath(resourceId: String) =
    Paths.get("src/awtMain/resources/$resourceId").normalize().toAbsolutePath().toString()

fun main() {
//    val p = resourcePath("NotoSansSC-Regular.ttf")
//    val typeface = Typeface.makeFromFile(p)
//
//    Clocks.fontCollection.setDefaultFontManager(TypefaceFontProvider().apply {
////        registerTypeface(typeface, "nssc")
//        registerTypeface(typeface, "test")
//    })
//    Clocks.fontCollection.setEnableFallback(true)


    val skiaLayer = SkiaLayer()
    val clocks = AwtClocks(skiaLayer)
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, clocks)
    SwingUtilities.invokeLater {
        val window = JFrame("Skiko example").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(800, 600)
        }
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true
    }
}
