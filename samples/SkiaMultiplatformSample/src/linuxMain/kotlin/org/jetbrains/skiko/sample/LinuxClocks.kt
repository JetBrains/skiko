@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.jetbrains.skiko.sample

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.FontRastrSettings
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skiko.FPSCounter
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoRenderDelegate
import org.jetbrains.skiko.currentSystemTheme
import platform.posix.getcwd
import platform.posix.getenv
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class LinuxClocks(
    private val scaleProvider: () -> Float,
    private val renderProvider: () -> GraphicsApi = { GraphicsApi.UNKNOWN },
) : SkikoRenderDelegate {
    constructor(layer: SkiaLayer) : this(
        { layer.contentScale },
        { layer.renderApi },
    )

    private val typeface = loadTypeface()
    private val font = Font(typeface, 13f).apply {
        edging = FontEdging.SUBPIXEL_ANTI_ALIAS
        hinting = FontHinting.SLIGHT
    }
    private val paint = Paint().apply {
        color = 0xff9BC730L.toInt()
        mode = PaintMode.FILL
        strokeWidth = 1f
        isAntiAlias = true
    }

    private var frame = 0
    private val fpsCounter: FPSCounter? = createFpsCounter()
    private var xpos = 0
    private var ypos = 0
    private val fontCollection = FontCollection()
        .setDefaultFontManager(FontMgr.default)

    fun motion(x: Int, y: Int) {
        xpos = x
        ypos = y
    }

    fun buttonPressed(button: Int, x: Int, y: Int, ctrl: Boolean) {
        xpos = x
        ypos = y
    }

    fun buttonReleased(button: Int) {
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        fpsCounter?.tick()

        val watchFill = Paint().apply { color = 0xFFFFFFFF.toInt() }
        val watchStroke = Paint().apply {
            color = Color.RED
            mode = PaintMode.STROKE
            strokeWidth = 1f
        }
        val watchStrokeAA = Paint().apply {
            color = 0xFF000000.toInt()
            mode = PaintMode.STROKE
            strokeWidth = 1f
        }
        val watchFillHover = Paint().apply { color = 0xFFE4FF01.toInt() }
        for (x in 0..(width - 50) step 50) {
            for (y in 20..(height - 50) step 50) {
                val hover = xpos > x + 0 && xpos < x + 50 && ypos > y + 0 && ypos < y + 50
                val fill = if (hover) watchFillHover else watchFill
                val stroke = if (x > width / 2) watchStrokeAA else watchStroke
                canvas.drawOval(Rect.makeXYWH(x + 5f, y + 5f, 40f, 40f), fill)
                canvas.drawOval(Rect.makeXYWH(x + 5f, y + 5f, 40f, 40f), stroke)
                var angle = 0f
                while (angle < 2f * PI) {
                    canvas.drawLine(
                        (x + 25 - 17 * sin(angle)),
                        (y + 25 + 17 * cos(angle)),
                        (x + 25 - 20 * sin(angle)),
                        (y + 25 + 20 * cos(angle)),
                        stroke,
                    )
                    angle += (2.0 * PI / 12.0).toFloat()
                }
                val time = (nanoTime / 1E6) % 60000 +
                    (x.toFloat() / width * 5000).toLong() +
                    (y.toFloat() / width * 5000).toLong()

                val angle1 = (time.toFloat() / 5000 * 2f * PI).toFloat()
                canvas.drawLine(
                    x + 25f,
                    y + 25f,
                    x + 25f - 15f * sin(angle1),
                    y + 25f + 15 * cos(angle1),
                    stroke,
                )

                val angle2 = (time / 60000 * 2f * PI).toFloat()
                canvas.drawLine(
                    x + 25f,
                    y + 25f,
                    x + 25f - 10f * sin(angle2),
                    y + 25f + 10f * cos(angle2),
                    stroke,
                )
            }
        }

        val text = "Frames: ${frame++}!"
        val x = xpos.toFloat() + 10f
        val y = ypos.toFloat() + 20f
        canvas.drawString(text, x, y, font, paint)

        val style = ParagraphStyle().apply {
            fontRastrSettings = FontRastrSettings(FontEdging.SUBPIXEL_ANTI_ALIAS, FontHinting.SLIGHT, true)
        }
        val paragraph = ParagraphBuilder(style, fontCollection)
            .pushStyle(TextStyle().setColor(0xFF000000.toInt()))
            .addText("Graphic API: ${renderProvider()}, Kotlin: ${KotlinVersion.CURRENT} $currentSystemTheme")
            .popStyle()
            .build()
        paragraph.layout(Float.POSITIVE_INFINITY)
        paragraph.paint(canvas, 5f, 5f)

        // Alpha layers test
        val rectW = 100f
        val rectH = 100f
        val scale = scaleProvider()
        val left = (width / scale - rectW) / 2f
        val top = (height / scale - rectH) / 2f
        val pictureRecorder = PictureRecorder()
        val pictureCanvas = pictureRecorder.beginRecording(
            Rect.makeLTRB(left, top, left + rectW, top + rectH),
        )
        pictureCanvas.drawLine(left, top, left + rectW, top + rectH, Paint())
        val picture = pictureRecorder.finishRecordingAsPicture()
        canvas.drawPicture(picture, null, Paint())
        canvas.drawLine(left, top + rectH, left + rectW, top, Paint())
    }

    private fun loadTypeface(): Typeface? {
        val candidates = listOf(
            "fonts/JetBrainsMono-Regular.ttf",
            "samples/SkiaAwtSample/fonts/JetBrainsMono-Regular.ttf",
            "skiko/src/commonTest/resources/fonts/JetBrainsMono-Regular.ttf",
            "skiko/src/commonTest/resources/fonts/JetBrainsMono_2_304/JetBrainsMono-Regular.ttf",
        )

        for (path in resolveFromWorkingDirectory(candidates)) {
            FontMgr.default.makeFromFile(path)?.let { return it }
        }

        return FontMgr.default.matchFamilyStyle("JetBrains Mono", FontStyle.NORMAL)
            ?: FontMgr.default.matchFamilyStyle("DejaVu Sans", FontStyle.NORMAL)
            ?: FontMgr.default.matchFamilyStyle("Sans", FontStyle.NORMAL)
    }

    private fun resolveFromWorkingDirectory(relativePaths: List<String>, maxParents: Int = 6): Sequence<String> {
        val cwd = currentWorkingDirectory() ?: return emptySequence()
        return sequence {
            var base: String? = cwd
            var remaining = maxParents
            while (base != null && remaining-- >= 0) {
                for (rel in relativePaths) {
                    yield("${base.trimEnd('/')}/${rel.trimStart('/')}")
                }
                base = parentDir(base)
            }
            for (rel in relativePaths) yield(rel)
        }
    }

    private fun currentWorkingDirectory(): String? = memScoped {
        val bufferSize = 4096
        val buffer = allocArray<ByteVar>(bufferSize)
        getcwd(buffer, bufferSize.convert())?.toKString()
    }

    private fun parentDir(path: String): String? {
        val normalized = path.trimEnd('/')
        val idx = normalized.lastIndexOf('/')
        return when {
            idx > 0 -> normalized.substring(0, idx)
            idx == 0 -> "/"
            else -> null
        }
    }

    private fun createFpsCounter(): FPSCounter? {
        if (!envFlag("SKIKO_FPS_ENABLED", defaultValue = false)) return null

        val periodSeconds = envDouble("SKIKO_FPS_PERIOD_SECONDS") ?: 2.0
        return FPSCounter(
            periodSeconds = periodSeconds,
            logOnTick = true,
        )
    }

    private fun envFlag(name: String, defaultValue: Boolean): Boolean {
        val raw = getenv(name)?.toKString()?.trim() ?: return defaultValue
        return when (raw.lowercase()) {
            "1", "true", "yes", "y", "on" -> true
            "0", "false", "no", "n", "off" -> false
            else -> defaultValue
        }
    }

    private fun envDouble(name: String): Double? =
        getenv(name)?.toKString()?.trim()?.toDoubleOrNull()
}
