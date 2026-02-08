/* ===============
 * SkijaGraphics2D
 * ===============
 *
 * (C)opyright 2021, by David Gilbert.
 *
 * The SkijaGraphics2D class has been developed by David Gilbert for
 * use with Orson Charts (http://www.object-refinery.com/orsoncharts) and
 * JFreeChart (http://www.jfree.org/jfreechart).  It may be useful for other
 * code that uses the Graphics2D API provided by Java2D.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the Object Refinery Limited nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL OBJECT REFINERY LIMITED BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.jetbrains.skiko.graphics2d

import org.jetbrains.skia.*
import org.jetbrains.skia.Canvas
import java.awt.*
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Image
import java.awt.MultipleGradientPaint.CycleMethod
import java.awt.Paint
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.font.TextLayout
import java.awt.geom.*
import java.awt.image.*
import java.awt.image.renderable.RenderableImage
import java.text.AttributedCharacterIterator
import org.jetbrains.skiko.Logger
import java.util.*
import java.util.function.Function

/**
 * An implementation of the Graphics2D API that targets the Skija graphics API
 * (https://github.com/JetBrains/skija).
 */
class SkikoGraphics2D : Graphics2D {
    /** Rendering hints.  */
    private val hints = RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_DEFAULT
    )
    /**
     * Returns the Skija surface that was created by this instance, or `null`.
     *
     * @return The Skija surface (possibly `null`).
     */
    /** Surface from Skija  */
    var surface: Surface? = null
        private set
    private var width = 0
    private var height = 0

    /** Canvas from Skija  */
    private var canvas: Canvas? = null

    /** Paint used for drawing on Skija canvas.  */
    private var skijaPaint: org.jetbrains.skia.Paint? = null

    /** The Skija save/restore count, used to restore the original clip in setClip().  */
    private var restoreCount = 0
    private var awtPaint: Paint? = null

    /** Stores the AWT Color object for get/setColor().  */
    private var color = Color.BLACK
    private var stroke: Stroke = BasicStroke(1.0f)
    private var awtFont = Font("SansSerif", Font.PLAIN, 12)
    private var typeface: Typeface? = null
    private val typefaceMap: MutableMap<TypefaceKey, Typeface?> = HashMap()
    private var skijaFont: org.jetbrains.skia.Font? = null

    /** The background color, used in the `clearRect()` method.  */
    private var background = Color.BLACK
    private var transform = AffineTransform()
    private var composite: Composite = AlphaComposite.getInstance(
        AlphaComposite.SRC_OVER, 1.0f
    )

    /** The user clip (can be null).  */
    private var clip: Shape? = null

    /**
     * The font render context.  The fractional metrics flag solves the glyph
     * positioning issue identified by Christoph Nahr:
     * http://news.kynosarges.org/2014/06/28/glyph-positioning-in-jfreesvg-orsonpdf/
     */
    private val fontRenderContext = FontRenderContext(
        null, false, true
    )

    /**
     * An instance that is lazily instantiated in drawLine and then
     * subsequently reused to avoid creating a lot of garbage.
     */
    private var line: Line2D? = null

    /**
     * An instance that is lazily instantiated in fillRect and then
     * subsequently reused to avoid creating a lot of garbage.
     */
    private var rect: Rectangle2D? = null

    /**
     * An instance that is lazily instantiated in draw/fillRoundRect and then
     * subsequently reused to avoid creating a lot of garbage.
     */
    private var roundRect: RoundRectangle2D? = null

    /**
     * An instance that is lazily instantiated in draw/fillOval and then
     * subsequently reused to avoid creating a lot of garbage.
     */
    private var oval: Ellipse2D? = null

    /**
     * An instance that is lazily instantiated in draw/fillArc and then
     * subsequently reused to avoid creating a lot of garbage.
     */
    private var arc: Arc2D? = null

    /**
     * The device configuration (this is lazily instantiated in the
     * getDeviceConfiguration() method).
     */
    private var deviceConfiguration: GraphicsConfiguration? = null
    private var fontMapping: Map<String, String?>? = null

    /**
     * Creates a new instance with the specified height and width.
     *
     * @param width  the width.
     * @param height  the height.
     */
    constructor(width: Int, height: Int) {
        Logger.debug { "SkijaGraphics2D($width, $height)" }
        this.width = width
        this.height = height
        surface = Surface.makeRasterN32Premul(width, height)
        fontMapping = createDefaultFontMap()
        setRenderingHint(
            SkikoHints.KEY_FONT_MAPPING_FUNCTION,
            Function { s: String -> fontMapping!![s] })
        init(surface!!.canvas)
    }

    /**
     * Creates a new instance with the specified height and width using an existing
     * canvas.
     *
     * @param canvas  the canvas (`null` not permitted).
     */
    constructor(canvas: Canvas?) {
        Logger.debug { "SkijaGraphics2D(Canvas)" }
        init(canvas)
    }

    /**
     * Creates a new instance using an existing canvas.
     *
     * @param canvas  the canvas (`null` not permitted).
     */
    private fun init(canvas: Canvas?) {
        nullNotPermitted(canvas, "canvas")
        this.canvas = canvas
        skijaPaint = org.jetbrains.skia.Paint()
        skijaPaint!!.setARGB(255, 0, 0, 0)

        typeface = FontMgr.default.legacyMakeTypeface(awtFont.name, FontStyle.NORMAL)
        skijaFont = org.jetbrains.skia.Font(typeface, 12.0f)

        // save the original clip settings so they can be restored later in setClip()
        restoreCount = this.canvas!!.save()
        Logger.debug { "restoreCount updated to $restoreCount" }
    }

    /** Used and reused in the path() method below.  */
    private val coords = DoubleArray(6)

    /**
     * Creates a Skija path from the outline of a Java2D shape.
     *
     * @param shape  the shape (`null` not permitted).
     *
     * @return A path.
     */
    private fun path(shape: Shape): Path {
        val p = Path()
        val iterator = shape.getPathIterator(null)
        while (!iterator.isDone) {
            val segType = iterator.currentSegment(coords)
            when (segType) {
                PathIterator.SEG_MOVETO -> {
                    Logger.debug { "SEG_MOVETO: ${coords[0]}, ${coords[1]}" }
                    p.moveTo(coords[0].toFloat(), coords[1].toFloat())
                }
                PathIterator.SEG_LINETO -> {
                    Logger.debug { "SEG_LINETO: ${coords[0]}, ${coords[1]}" }
                    p.lineTo(coords[0].toFloat(), coords[1].toFloat())
                }
                PathIterator.SEG_QUADTO -> {
                    Logger.debug { "SEG_QUADTO: ${coords[0]}, ${coords[1]}, ${coords[2]}, ${coords[3]}" }
                    p.quadTo(
                        coords[0].toFloat(),
                        coords[1].toFloat(),
                        coords[2].toFloat(),
                        coords[3].toFloat()
                    )
                }
                PathIterator.SEG_CUBICTO -> {
                    Logger.debug { "SEG_CUBICTO: ${coords[0]}, ${coords[1]}, ${coords[2]}, ${coords[3]}, ${coords[4]}, ${coords[5]}" }
                    p.cubicTo(
                        coords[0].toFloat(),
                        coords[1].toFloat(),
                        coords[2].toFloat(),
                        coords[3].toFloat(),
                        coords[4].toFloat(),
                        coords[5].toFloat()
                    )
                }
                PathIterator.SEG_CLOSE -> {
                    Logger.debug { "SEG_CLOSE" }
                    p.closePath()
                }
                else -> throw RuntimeException(
                    "Unrecognised segment type "
                            + segType
                )
            }
            iterator.next()
        }
        return p
    }

    /**
     * Draws the specified shape with the current `paint` and
     * `stroke`.  There is direct handling for `Line2D` and
     * `Rectangle2D`.  All other shapes are mapped to a `GeneralPath`
     * and then drawn (effectively as `Path2D` objects).
     *
     * @param s  the shape (`null` not permitted).
     *
     * @see .fill
     */
    override fun draw(s: Shape) {
        Logger.debug { "draw(Shape) : $s" }
        skijaPaint!!.mode = PaintMode.STROKE
        if (s is Line2D) {
            val l = s
            canvas!!.drawLine(
                l.x1.toFloat(),
                l.y1.toFloat(),
                l.x2.toFloat(),
                l.y2.toFloat(),
                skijaPaint!!
            )
        } else if (s is Rectangle2D) {
            val r = s
            if (r.width < 0.0 || r.height < 0.0) {
                return
            }
            canvas!!.drawRect(
                Rect.makeXYWH(
                    r.x.toFloat(),
                    r.y.toFloat(),
                    r.width.toFloat(),
                    r.height.toFloat()
                ), skijaPaint!!
            )
        } else if (s is Ellipse2D) {
            val e = s
            canvas!!.drawOval(
                Rect.makeXYWH(
                    e.minX.toFloat(),
                    e.minY.toFloat(),
                    e.width.toFloat(),
                    e.height.toFloat()
                ), skijaPaint!!
            )
        } else {
            canvas!!.drawPath(path(s), skijaPaint!!)
        }
    }

    /**
     * Fills the specified shape with the current `paint`.  There is
     * direct handling for `Rectangle2D`.
     * All other shapes are mapped to a path outline and then filled.
     *
     * @param s  the shape (`null` not permitted).
     *
     * @see .draw
     */
    override fun fill(s: Shape) {
        Logger.debug { "fill($s)" }
        skijaPaint!!.mode = PaintMode.FILL
        if (s is Rectangle2D) {
            val r = s
            if (r.width < 0.0 || r.height < 0.0) {
                return
            }
            canvas!!.drawRect(
                Rect.makeXYWH(
                    r.x.toFloat(),
                    r.y.toFloat(),
                    r.width.toFloat(),
                    r.height.toFloat()
                ), skijaPaint!!
            )
        } else if (s is Ellipse2D) {
            val e = s
            canvas!!.drawOval(
                Rect.makeXYWH(
                    e.minX.toFloat(),
                    e.minY.toFloat(),
                    e.width.toFloat(),
                    e.height.toFloat()
                ), skijaPaint!!
            )
        } else if (s is Path2D) {
            val path = path(s)
            if (s.windingRule == Path2D.WIND_EVEN_ODD) {
                path.fillMode = PathFillMode.EVEN_ODD
            } else {
                path.fillMode = PathFillMode.WINDING
            }
            canvas!!.drawPath(path, skijaPaint!!)
        } else {
            canvas!!.drawPath(path(s), skijaPaint!!)
        }
    }

    /**
     * Draws an image with the specified transform. Note that the
     * `observer` is ignored in this implementation.
     *
     * @param img  the image.
     * @param xform  the transform (`null` permitted).
     * @param obs  the image observer (ignored).
     *
     * @return `true` if the image is drawn.
     */
    override fun drawImage(
        img: Image?, xform: AffineTransform?,
        obs: ImageObserver?
    ): Boolean {
        Logger.debug { "drawImage(Image, AffineTransform, ImageObserver)" }
        val savedTransform = getTransform()
        if (xform != null) {
            transform(xform)
        }
        val result = drawImage(img, 0, 0, obs)
        if (xform != null) {
            setTransform(savedTransform)
        }
        return result
    }

    /**
     * Draws the image resulting from applying the `BufferedImageOp`
     * to the specified image at the location `(x, y)`.
     *
     * @param img  the image.
     * @param op  the operation (`null` permitted).
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     */
    override fun drawImage(img: BufferedImage?, op: BufferedImageOp?, x: Int, y: Int) {
        Logger.debug { "drawImage(BufferedImage, BufferedImageOp, $x, $y)" }
        var imageToDraw = img
        if (op != null) {
            imageToDraw = op.filter(img, null)
        }
        drawImage(imageToDraw, AffineTransform(1.0, 0.0, 0.0, 1.0, x.toDouble(), y.toDouble()), null)
    }

    /**
     * Draws the rendered image. When `img` is `null` this method
     * does nothing.
     *
     * @param img  the image (`null` permitted).
     * @param xform  the transform.
     */
    override fun drawRenderedImage(img: RenderedImage?, xform: AffineTransform?) {
        Logger.debug { "drawRenderedImage(RenderedImage, AffineTransform)" }
        if (img == null) { // to match the behaviour specified in the JDK
            return
        }
        val bi = convertRenderedImage(img)
        drawImage(bi, xform, null)
    }

    /**
     * Draws the renderable image.
     *
     * @param img  the renderable image.
     * @param xform  the transform.
     */
    override fun drawRenderableImage(
        img: RenderableImage,
        xform: AffineTransform?
    ) {
        Logger.debug { "drawRenderableImage(RenderableImage, AffineTransform xform)" }
        val ri = img.createDefaultRendering()
        drawRenderedImage(ri, xform)
    }

    /**
     * Draws a string at `(x, y)`.  The start of the text at the
     * baseline level will be aligned with the `(x, y)` point.
     *
     * @param str  the string (`null` not permitted).
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     *
     * @see .drawString
     */
    override fun drawString(str: String, x: Int, y: Int) {
        Logger.debug { "drawString($str, $x, $y)" }
        drawString(str, x.toFloat(), y.toFloat())
    }

    /**
     * Draws a string at `(x, y)`. The start of the text at the
     * baseline level will be aligned with the `(x, y)` point.
     *
     * @param str  the string (`null` not permitted).
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     */
    override fun drawString(str: String, x: Float, y: Float) {
        if (str == null) {
            throw NullPointerException("Null 'str' argument.")
        }
        Logger.debug { "drawString($str, $x, $y)" }
        skijaPaint!!.mode = PaintMode.FILL
        canvas!!.drawString(str, x, y, skijaFont, skijaPaint!!)
    }

    /**
     * Draws a string of attributed characters at `(x, y)`.  The
     * call is delegated to
     * [.drawString].
     *
     * @param iterator  an iterator for the characters.
     * @param x  the x-coordinate.
     * @param y  the x-coordinate.
     */
    override fun drawString(iterator: AttributedCharacterIterator, x: Int, y: Int) {
        Logger.debug { "drawString(AttributedCharacterIterator, $x, $y)" }
        drawString(iterator, x.toFloat(), y.toFloat())
    }

    /**
     * Draws a string of attributed characters at `(x, y)`.
     *
     * @param iterator  an iterator over the characters (`null` not
     * permitted).
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     */
    override fun drawString(
        iterator: AttributedCharacterIterator, x: Float,
        y: Float
    ) {
        Logger.debug { "drawString(AttributedCharacterIterator, $x, $y)" }
        val s = iterator.allAttributeKeys
        if (!s.isEmpty()) {
            val layout = TextLayout(
                iterator,
                getFontRenderContext()
            )
            layout.draw(this, x, y)
        } else {
            val strb = StringBuilder()
            iterator.first()
            for (i in iterator.beginIndex until iterator.endIndex) {
                strb.append(iterator.current())
                iterator.next()
            }
            drawString(strb.toString(), x, y)
        }
    }

    /**
     * Draws the specified glyph vector at the location `(x, y)`.
     *
     * @param g  the glyph vector (`null` not permitted).
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     */
    override fun drawGlyphVector(g: GlyphVector, x: Float, y: Float) {
        Logger.debug { "drawGlyphVector(GlyphVector, $x, $y)" }
        fill(g.getOutline(x, y))
    }

    /**
     * Returns `true` if the rectangle (in device space) intersects
     * with the shape (the interior, if `onStroke` is `false`,
     * otherwise the stroked outline of the shape).
     *
     * @param rect  a rectangle (in device space).
     * @param s the shape.
     * @param onStroke  test the stroked outline only?
     *
     * @return A boolean.
     */
    override fun hit(rect: Rectangle, s: Shape, onStroke: Boolean): Boolean {
        Logger.debug { "hit(Rectangle, Shape, boolean)" }
        val ts: Shape
        ts = if (onStroke) {
            transform.createTransformedShape(
                stroke.createStrokedShape(s)
            )
        } else {
            transform.createTransformedShape(s)
        }
        if (!rect.bounds2D.intersects(ts.bounds2D)) {
            return false
        }
        val a1 = Area(rect)
        val a2 = Area(ts)
        a1.intersect(a2)
        return !a1.isEmpty
    }

    /**
     * Returns the device configuration associated with this
     * `Graphics2D`.
     *
     * @return The device configuration (never `null`).
     */
    override fun getDeviceConfiguration(): GraphicsConfiguration {
        if (deviceConfiguration == null) {
            val width = width
            val height = height
            deviceConfiguration = SkikoGraphicsConfiguration(
                width,
                height
            )
        }
        return deviceConfiguration!!
    }

    /**
     * Sets the composite (only `AlphaComposite` is handled).
     *
     * @param comp  the composite (`null` not permitted).
     *
     * @see .getComposite
     */
    override fun setComposite(comp: Composite?) {
        Logger.debug { "setComposite($comp)" }
        requireNotNull(comp) { "Null 'comp' argument." }
        composite = comp
        if (comp is AlphaComposite) {
            val ac = comp
            skijaPaint!!.setAlphaf(ac.alpha)
            when (ac.rule) {
                AlphaComposite.CLEAR -> skijaPaint!!.blendMode = BlendMode.CLEAR
                AlphaComposite.SRC -> skijaPaint!!.blendMode = BlendMode.SRC
                AlphaComposite.SRC_OVER -> skijaPaint!!.blendMode = BlendMode.SRC_OVER
                AlphaComposite.DST_OVER -> skijaPaint!!.blendMode = BlendMode.DST_OVER
                AlphaComposite.SRC_IN -> skijaPaint!!.blendMode = BlendMode.SRC_IN
                AlphaComposite.DST_IN -> skijaPaint!!.blendMode = BlendMode.DST_IN
                AlphaComposite.SRC_OUT -> skijaPaint!!.blendMode = BlendMode.SRC_OUT
                AlphaComposite.DST_OUT -> skijaPaint!!.blendMode = BlendMode.DST_OUT
                AlphaComposite.DST -> skijaPaint!!.blendMode = BlendMode.DST
                AlphaComposite.SRC_ATOP -> skijaPaint!!.blendMode = BlendMode.SRC_ATOP
                AlphaComposite.DST_ATOP -> skijaPaint!!.blendMode = BlendMode.DST_ATOP
            }
        }
    }

    override fun setPaint(paint: Paint?) {
        Logger.debug { "setPaint($paint)" }
        if (paint == null) {
            return
        }
        if (paintsAreEqual(paint, awtPaint)) {
            return
        }
        awtPaint = paint
        if (paint is Color) {
            val c = paint
            color = c
            skijaPaint!!.shader = Shader.makeColor(c.rgb)
        } else if (paint is LinearGradientPaint) {
            val lgp = paint
            val x0 = lgp.startPoint.x.toFloat()
            val y0 = lgp.startPoint.y.toFloat()
            val x1 = lgp.endPoint.x.toFloat()
            val y1 = lgp.endPoint.y.toFloat()
            val colors = IntArray(lgp.colors.size)
            for (i in lgp.colors.indices) {
                colors[i] = lgp.colors[i].rgb
            }
            val fractions = lgp.fractions
            val gs =
                GradientStyle.DEFAULT.withTileMode(awtCycleMethodToSkijaFilterTileMode(lgp.cycleMethod))
            val shader = Shader.makeLinearGradient(x0, y0, x1, y1, colors, fractions, gs)
            skijaPaint!!.shader = shader
        } else if (paint is RadialGradientPaint) {
            val rgp = paint
            val x = rgp.centerPoint.x.toFloat()
            val y = rgp.centerPoint.y.toFloat()
            val colors = IntArray(rgp.colors.size)
            for (i in rgp.colors.indices) {
                colors[i] = rgp.colors[i].rgb
            }
            val gs =
                GradientStyle.DEFAULT.withTileMode(awtCycleMethodToSkijaFilterTileMode(rgp.cycleMethod))
            val fx = rgp.focusPoint.x.toFloat()
            val fy = rgp.focusPoint.y.toFloat()
            val shader: Shader
            shader = if (rgp.focusPoint == rgp.centerPoint) {
                Shader.makeRadialGradient(x, y, rgp.radius, colors, rgp.fractions, gs)
            } else {
                Shader.makeTwoPointConicalGradient(
                    fx,
                    fy,
                    0f,
                    x,
                    y,
                    rgp.radius,
                    colors,
                    rgp.fractions,
                    gs
                )
            }
            skijaPaint!!.shader = shader
        } else if (paint is GradientPaint) {
            val gp = paint
            val x1 = gp.point1.x.toFloat()
            val y1 = gp.point1.y.toFloat()
            val x2 = gp.point2.x.toFloat()
            val y2 = gp.point2.y.toFloat()
            val colors = intArrayOf(gp.color1.rgb, gp.color2.rgb)
            var gs = GradientStyle.DEFAULT
            if (gp.isCyclic) {
                gs = GradientStyle.DEFAULT.withTileMode(FilterTileMode.MIRROR)
            }
            val shader = Shader.makeLinearGradient(x1, y1, x2, y2, colors, null as FloatArray?, gs)
            skijaPaint!!.shader = shader
        }
    }

    /**
     * Sets the stroke that will be used to draw shapes.
     *
     * @param s  the stroke (`null` not permitted).
     *
     * @see .getStroke
     */
    override fun setStroke(s: Stroke) {
        nullNotPermitted(s, "s")
        Logger.debug { "setStroke($stroke)" }
        if (s === stroke) { // quick test, full equals test later
            return
        }
        if (stroke is BasicStroke) {
            val bs = s as BasicStroke
            if (bs == stroke) {
                return  // no change
            }
            val lineWidth = bs.lineWidth.toDouble()
            skijaPaint!!.strokeWidth =
                Math.max(lineWidth, MIN_LINE_WIDTH).toFloat()
            skijaPaint!!.strokeCap = awtToSkijaLineCap(bs.endCap)
            skijaPaint!!.strokeJoin = awtToSkijaLineJoin(bs.lineJoin)
            skijaPaint!!.strokeMiter = bs.miterLimit
            if (bs.dashArray != null) {
                skijaPaint!!.pathEffect = PathEffect.makeDash(bs.dashArray, bs.dashPhase)
            } else {
                skijaPaint!!.pathEffect = null
            }
        }
        stroke = s
    }

    /**
     * Maps a line cap code from AWT to the corresponding Skija `PaintStrokeCap`
     * enum value.
     *
     * @param c  the line cap code.
     *
     * @return A Skija stroke cap value.
     */
    private fun awtToSkijaLineCap(c: Int): PaintStrokeCap {
        return if (c == BasicStroke.CAP_BUTT) {
            PaintStrokeCap.BUTT
        } else if (c == BasicStroke.CAP_ROUND) {
            PaintStrokeCap.ROUND
        } else if (c == BasicStroke.CAP_SQUARE) {
            PaintStrokeCap.SQUARE
        } else {
            throw IllegalArgumentException("Unrecognised cap code: $c")
        }
    }

    /**
     * Maps a line join code from AWT to the corresponding Skija
     * `PaintStrokeJoin` enum value.
     *
     * @param j  the line join code.
     *
     * @return A Skija stroke join value.
     */
    private fun awtToSkijaLineJoin(j: Int): PaintStrokeJoin {
        return if (j == BasicStroke.JOIN_BEVEL) {
            PaintStrokeJoin.BEVEL
        } else if (j == BasicStroke.JOIN_MITER) {
            PaintStrokeJoin.MITER
        } else if (j == BasicStroke.JOIN_ROUND) {
            PaintStrokeJoin.ROUND
        } else {
            throw IllegalArgumentException("Unrecognised join code: $j")
        }
    }

    /**
     * Maps a linear gradient paint cycle method from AWT to the corresponding Skija
     * `FilterTileMode` enum value.
     *
     * @param method  the cycle method.
     *
     * @return A Skija stroke join value.
     */
    private fun awtCycleMethodToSkijaFilterTileMode(method: CycleMethod): FilterTileMode {
        return when (method) {
            CycleMethod.NO_CYCLE -> FilterTileMode.CLAMP
            CycleMethod.REPEAT -> FilterTileMode.REPEAT
            CycleMethod.REFLECT -> FilterTileMode.MIRROR
            else -> FilterTileMode.CLAMP
        }
    }

    /**
     * Returns the current value for the specified hint.  Note that all hints
     * are currently ignored in this implementation.
     *
     * @param hintKey  the hint key (`null` permitted, but the
     * result will be `null` also in that case).
     *
     * @return The current value for the specified hint
     * (possibly `null`).
     *
     * @see .setRenderingHint
     */
    override fun getRenderingHint(hintKey: RenderingHints.Key): Any {
        Logger.debug { "getRenderingHint($hintKey)" }
        return hints[hintKey]!!
    }

    /**
     * Sets the value for a hint.  See the `FXHints` class for
     * information about the hints that can be used with this implementation.
     *
     * @param hintKey  the hint key (`null` not permitted).
     * @param hintValue  the hint value.
     *
     * @see .getRenderingHint
     */
    override fun setRenderingHint(hintKey: RenderingHints.Key, hintValue: Any) {
        Logger.debug { "setRenderingHint($hintKey, $hintValue)" }
        hints[hintKey] = hintValue
    }

    /**
     * Sets the rendering hints to the specified collection.
     *
     * @param hints  the new set of hints (`null` not permitted).
     *
     * @see .getRenderingHints
     */
    override fun setRenderingHints(hints: Map<*, *>?) {
        Logger.debug { "setRenderingHints(Map<?, ?>)" }
        this.hints.clear()
        this.hints.putAll(hints!!)
    }

    /**
     * Adds all the supplied rendering hints.
     *
     * @param hints  the hints (`null` not permitted).
     */
    override fun addRenderingHints(hints: Map<*, *>?) {
        Logger.debug { "addRenderingHints(Map<?, ?>)" }
        this.hints.putAll(hints!!)
    }

    /**
     * Returns a copy of the rendering hints.  Modifying the returned copy
     * will have no impact on the state of this `Graphics2D`
     * instance.
     *
     * @return The rendering hints (never `null`).
     *
     * @see .setRenderingHints
     */
    override fun getRenderingHints(): RenderingHints {
        Logger.debug { "getRenderingHints()" }
        return hints.clone() as RenderingHints
    }

    /**
     * Applies the translation `(tx, ty)`.  This call is delegated
     * to [.translate].
     *
     * @param tx  the x-translation.
     * @param ty  the y-translation.
     *
     * @see .translate
     */
    override fun translate(tx: Int, ty: Int) {
        Logger.debug { "translate($tx, $ty)" }
        translate(tx.toDouble(), ty.toDouble())
    }

    /**
     * Applies the translation `(tx, ty)`.
     *
     * @param tx  the x-translation.
     * @param ty  the y-translation.
     */
    override fun translate(tx: Double, ty: Double) {
        Logger.debug { "translate($tx, $ty)" }
        transform.translate(tx, ty)
        canvas!!.translate(tx.toFloat(), ty.toFloat())
    }

    /**
     * Applies a rotation (anti-clockwise) about `(0, 0)`.
     *
     * @param theta  the rotation angle (in radians).
     */
    override fun rotate(theta: Double) {
        Logger.debug { "rotate($theta)" }
        transform.rotate(theta)
        canvas!!.rotate(Math.toDegrees(theta).toFloat())
    }

    /**
     * Applies a rotation (anti-clockwise) about `(x, y)`.
     *
     * @param theta  the rotation angle (in radians).
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     */
    override fun rotate(theta: Double, x: Double, y: Double) {
        Logger.debug { "rotate($theta, $x, $y)" }
        translate(x, y)
        rotate(theta)
        translate(-x, -y)
    }

    /**
     * Applies a scale transformation.
     *
     * @param sx  the x-scaling factor.
     * @param sy  the y-scaling factor.
     */
    override fun scale(sx: Double, sy: Double) {
        Logger.debug { "scale($sx, $sy)" }
        transform.scale(sx, sy)
        canvas!!.scale(sx.toFloat(), sy.toFloat())
    }

    /**
     * Applies a shear transformation. This is equivalent to the following
     * call to the `transform` method:
     * <br></br><br></br>
     *  *
     * `transform(AffineTransform.getShearInstance(shx, shy));`
     *
     *
     * @param shx  the x-shear factor.
     * @param shy  the y-shear factor.
     */
    override fun shear(shx: Double, shy: Double) {
        Logger.debug { "shear($shx, $shy)" }
        transform.shear(shx, shy)
        canvas!!.skew(shx.toFloat(), shy.toFloat())
    }

    /**
     * Applies this transform to the existing transform by concatenating it.
     *
     * @param t  the transform (`null` not permitted).
     */
    override fun transform(t: AffineTransform) {
        Logger.debug { "transform(AffineTransform) : $t" }
        val tx = getTransform()
        tx.concatenate(t)
        setTransform(tx)
    }

    /**
     * Returns a copy of the current transform.
     *
     * @return A copy of the current transform (never `null`).
     *
     * @see .setTransform
     */
    override fun getTransform(): AffineTransform {
        Logger.debug { "getTransform()" }
        return transform.clone() as AffineTransform
    }

    /**
     * Sets the transform.
     *
     * @param t  the new transform (`null` permitted, resets to the
     * identity transform).
     *
     * @see .getTransform
     */
    override fun setTransform(t: AffineTransform) {
        var t: AffineTransform? = t
        Logger.debug { "setTransform($t)" }
        if (t == null) {
            transform = AffineTransform()
            t = transform
        } else {
            transform = AffineTransform(t)
        }
        val m33 = Matrix33(
            t.scaleX.toFloat(),
            t.shearX.toFloat(),
            t.translateX.toFloat(),
            t.shearY.toFloat(),
            t.scaleY.toFloat(),
            t.translateY.toFloat(),
            0f,
            0f,
            1f
        )
        canvas!!.setMatrix(m33)
    }

    override fun getPaint(): Paint {
        return awtPaint!!
    }

    /**
     * Returns the current composite.
     *
     * @return The current composite (never `null`).
     *
     * @see .setComposite
     */
    override fun getComposite(): Composite {
        return composite
    }

    /**
     * Returns the background color (the default value is [Color.BLACK]).
     * This attribute is used by the [.clearRect]
     * method.
     *
     * @return The background color (possibly `null`).
     *
     * @see .setBackground
     */
    override fun getBackground(): Color {
        return background
    }

    /**
     * Sets the background color.  This attribute is used by the
     * [.clearRect] method.  The reference
     * implementation allows `null` for the background color so
     * we allow that too (but for that case, the [.clearRect]
     * method will do nothing).
     *
     * @param color  the color (`null` permitted).
     *
     * @see .getBackground
     */
    override fun setBackground(color: Color?) {
        background = color
    }

    /**
     * Returns the current stroke (this attribute is used when drawing shapes).
     *
     * @return The current stroke (never `null`).
     *
     * @see .setStroke
     */
    override fun getStroke(): Stroke {
        return stroke
    }

    /**
     * Returns the font render context.
     *
     * @return The font render context (never `null`).
     */
    override fun getFontRenderContext(): FontRenderContext {
        return fontRenderContext
    }

    /**
     * Creates a new graphics object that is a copy of this graphics object.
     *
     * @return A new graphics object.
     */
    override fun create(): Graphics2D {
        Logger.debug { "create()" }
        val copy = SkikoGraphics2D(canvas)
        copy.setRenderingHints(renderingHints)
        copy.clip = clip
        copy.paint = paint
        copy.setColor(getColor())
        copy.setComposite(getComposite())
        copy.setStroke(getStroke())
        copy.font = font
        copy.setTransform(getTransform())
        copy.setBackground(getBackground())
        return copy
    }

    override fun create(x: Int, y: Int, width: Int, height: Int): java.awt.Graphics {
        Logger.debug { "create($x, $y, $width, $height)" }
        return super.create(x, y, width, height)
    }

    /**
     * Returns the foreground color.  This method exists for backwards
     * compatibility in AWT, you should use the [.getPaint] method.
     * This attribute is updated by the [.setColor]
     * method, and also by the [.setPaint] method if
     * a `Color` instance is passed to the method.
     *
     * @return The foreground color (never `null`).
     *
     * @see .getPaint
     */
    override fun getColor(): Color {
        return color
    }

    /**
     * Sets the foreground color.  This method exists for backwards
     * compatibility in AWT, you should use the
     * [.setPaint] method.
     *
     * @param c  the color (`null` permitted but ignored).
     *
     * @see .setPaint
     */
    override fun setColor(c: Color?) {
        Logger.debug { "setColor(Color) : $c" }
        if (c == null || c == color) {
            return
        }
        color = c
        paint = c
    }

    /**
     * Not implemented - the method does nothing.
     */
    override fun setPaintMode() {
        // not implemented
    }

    /**
     * Not implemented - the method does nothing.
     */
    override fun setXORMode(c1: Color) {
        // not implemented
    }

    /**
     * Returns the current font used for drawing text.
     *
     * @return The current font (never `null`).
     *
     * @see .setFont
     */
    override fun getFont(): Font {
        return awtFont
    }

    private fun awtFontStyleToSkijaFontStyle(style: Int): FontStyle {
        return if (style == Font.PLAIN) {
            FontStyle.NORMAL
        } else if (style == Font.BOLD) {
            FontStyle.BOLD
        } else if (style == Font.ITALIC) {
            FontStyle.ITALIC
        } else if (style == Font.BOLD + Font.ITALIC) {
            FontStyle.BOLD_ITALIC
        } else {
            FontStyle.NORMAL
        }
    }

    /**
     * Sets the font to be used for drawing text.
     *
     * @param font  the font (`null` is permitted but ignored).
     *
     * @see .getFont
     */
    override fun setFont(font: Font) {
        Logger.debug { "setFont($font)" }
        if (font == null) {
            return
        }
        awtFont = font
        var fontName = font.name
        // check if there is a font name mapping to apply
        val fontMapping = getRenderingHint(SkikoHints.KEY_FONT_MAPPING_FUNCTION) as Function<String, String>
        if (fontMapping != null) {
            val mappedFontName = fontMapping.apply(fontName)
            if (mappedFontName != null) {
                Logger.debug { "Mapped font name is $mappedFontName" }
                fontName = mappedFontName
            }
        }
        val style = awtFontStyleToSkijaFontStyle(font.style)
        val key = TypefaceKey(fontName!!, style)
        typeface = typefaceMap[key]
        if (typeface == null) {
            typeface = FontMgr.default.legacyMakeTypeface(fontName, awtFontStyleToSkijaFontStyle(font.style))
            typefaceMap[key] = typeface
        }
        skijaFont = org.jetbrains.skia.Font(typeface, font.size.toFloat())
    }

    /**
     * Returns the font metrics for the specified font.
     *
     * @param f  the font.
     *
     * @return The font metrics.
     */
    override fun getFontMetrics(f: Font): FontMetrics {
        return SkikoFontMetrics(skijaFont, awtFont)
    }

    /**
     * Returns the bounds of the user clipping region.
     *
     * @return The clip bounds (possibly `null`).
     *
     * @see .getClip
     */
    override fun getClipBounds(): Rectangle? {
        return if (clip == null) {
            null
        } else getClip()?.bounds
    }

    /**
     * Returns the user clipping region.  The initial default value is
     * `null`.
     *
     * @return The user clipping region (possibly `null`).
     *
     * @see .setClip
     */
    override fun getClip(): Shape? {
        Logger.debug { "getClip()" }
        return if (clip == null) {
            null
        } else try {
            val inv = transform.createInverse()
            inv.createTransformedShape(clip)
        } catch (ex: NoninvertibleTransformException) {
            null
        }
    }

    /**
     * Sets the user clipping region.
     *
     * @param shape  the new user clipping region (`null` permitted).
     *
     * @see .getClip
     */
    override fun setClip(shape: Shape?) {
        Logger.debug { "setClip($shape)" }
        // null is handled fine here...
        // a new clip is being set, so first restore the original clip (and save
        // it again for future restores)
        canvas!!.restoreToCount(restoreCount)
        restoreCount = canvas!!.save()
        // restoring the clip might also reset the transform, so reapply it
        setTransform(getTransform())
        clip = transform.createTransformedShape(shape)
        // now apply on the Skija canvas
        if (shape != null) {
            canvas!!.clipPath(path(shape))
        }
    }

    /**
     * Clips to the intersection of the current clipping region and the
     * specified rectangle.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     */
    override fun clipRect(x: Int, y: Int, width: Int, height: Int) {
        Logger.debug { "clipRect($x, $y, $width, $height)" }
        clip(rect(x, y, width, height)!!)
    }

    /**
     * Sets the user clipping region to the specified rectangle.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     *
     * @see .getClip
     */
    override fun setClip(x: Int, y: Int, width: Int, height: Int) {
        Logger.debug { "setClip($x, $y, $width, $height)" }
        setClip(rect(x, y, width, height)!!)
    }

    /**
     * Clips to the intersection of the current clipping region and the
     * specified shape.
     *
     * According to the Oracle API specification, this method will accept a
     * `null` argument, but there is an open bug report (since 2004)
     * that suggests this is wrong:
     *
     *
     * [
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6206189](http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6206189)
     *
     * @param s  the clip shape (`null` not permitted).
     */
    override fun clip(s: Shape) {
        var s = s
        Logger.debug { "clip($s)" }
        if (s is Line2D) {
            s = s.getBounds2D()
        }
        if (clip == null) {
            setClip(s)
            return
        }
        if (!s.intersects(getClip()?.bounds2D)) {
            setClip(Rectangle2D.Double())
        } else {
            val a1 = Area(s)
            val a2 = Area(getClip())
            a1.intersect(a2)
            setClip(Path2D.Double(a1))
            canvas!!.clipPath(path(s))
        }
    }

    /**
     * Not yet implemented.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width of the area.
     * @param height  the height of the area.
     * @param dx  the delta x.
     * @param dy  the delta y.
     */
    override fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int) {
        Logger.debug { "copyArea($x, $y, $width, $height, $dx, $dy) - NOT IMPLEMENTED" }
        // FIXME: implement this, low priority
    }

    /**
     * Draws a line from `(x1, y1)` to `(x2, y2)` using
     * the current `paint` and `stroke`.
     *
     * @param x1  the x-coordinate of the start point.
     * @param y1  the y-coordinate of the start point.
     * @param x2  the x-coordinate of the end point.
     * @param y2  the x-coordinate of the end point.
     */
    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        Logger.debug { "drawLine()" }
        if (line == null) {
            line = Line2D.Double(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
        } else {
            line!!.setLine(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
        }
        draw(line!!)
    }

    /**
     * Fills the specified rectangle with the current `paint`.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the rectangle width.
     * @param height  the rectangle height.
     */
    override fun fillRect(x: Int, y: Int, width: Int, height: Int) {
        Logger.debug { "fillRect($x, $y, $width, $height)" }
        fill(rect(x, y, width, height)!!)
    }

    /**
     * Clears the specified rectangle by filling it with the current
     * background color.  If the background color is `null`, this
     * method will do nothing.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     *
     * @see .getBackground
     */
    override fun clearRect(x: Int, y: Int, width: Int, height: Int) {
        Logger.debug { "clearRect($x, $y, $width, $height)" }
        if (getBackground() == null) {
            return  // we can't do anything
        }
        val saved = paint
        paint = getBackground()
        fillRect(x, y, width, height)
        paint = saved
    }

    /**
     * Sets the attributes of the reusable [Rectangle2D] object that is
     * used by the [SkijaGraphics2D.drawRect] and
     * [SkikoGraphics2D.fillRect] methods.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     *
     * @return A rectangle (never `null`).
     */
    private fun rect(x: Int, y: Int, width: Int, height: Int): Rectangle2D? {
        if (rect == null) {
            rect =
                Rectangle2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        } else {
            rect!!.setRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        }
        return rect
    }

    /**
     * Draws a rectangle with rounded corners using the current
     * `paint` and `stroke`.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     * @param arcWidth  the arc-width.
     * @param arcHeight  the arc-height.
     *
     * @see .fillRoundRect
     */
    override fun drawRoundRect(
        x: Int, y: Int, width: Int, height: Int,
        arcWidth: Int, arcHeight: Int
    ) {
        Logger.debug { "drawRoundRect($x, $y, $width, $height, $arcWidth, $arcHeight)" }
        draw(roundRect(x, y, width, height, arcWidth, arcHeight)!!)
    }

    /**
     * Fills a rectangle with rounded corners using the current `paint`.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     * @param arcWidth  the arc-width.
     * @param arcHeight  the arc-height.
     *
     * @see .drawRoundRect
     */
    override fun fillRoundRect(
        x: Int, y: Int, width: Int, height: Int,
        arcWidth: Int, arcHeight: Int
    ) {
        Logger.debug { "fillRoundRect($x, $y, $width, $height, $arcWidth, $arcHeight)" }
        fill(roundRect(x, y, width, height, arcWidth, arcHeight)!!)
    }

    /**
     * Sets the attributes of the reusable [RoundRectangle2D] object that
     * is used by the [.drawRoundRect] and
     * [.fillRoundRect] methods.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     * @param arcWidth  the arc width.
     * @param arcHeight  the arc height.
     *
     * @return A round rectangle (never `null`).
     */
    private fun roundRect(
        x: Int, y: Int, width: Int, height: Int,
        arcWidth: Int, arcHeight: Int
    ): RoundRectangle2D? {
        if (roundRect == null) {
            roundRect = RoundRectangle2D.Double(
                x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(),
                arcWidth.toDouble(), arcHeight.toDouble()
            )
        } else {
            roundRect!!.setRoundRect(
                x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(),
                arcWidth.toDouble(), arcHeight.toDouble()
            )
        }
        return roundRect
    }

    /**
     * Draws an oval framed by the rectangle `(x, y, width, height)`
     * using the current `paint` and `stroke`.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     *
     * @see .fillOval
     */
    override fun drawOval(x: Int, y: Int, width: Int, height: Int) {
        Logger.debug { "drawOval($x, $y, $width, $height)" }
        draw(oval(x, y, width, height)!!)
    }

    /**
     * Fills an oval framed by the rectangle `(x, y, width, height)`.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     *
     * @see .drawOval
     */
    override fun fillOval(x: Int, y: Int, width: Int, height: Int) {
        Logger.debug { "fillOval($x, $y, $width, $height)" }
        fill(oval(x, y, width, height)!!)
    }

    /**
     * Returns an [Ellipse2D] object that may be reused (so this instance
     * should be used for short term operations only). See the
     * [.drawOval] and
     * [.fillOval] methods for usage.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     *
     * @return An oval shape (never `null`).
     */
    private fun oval(x: Int, y: Int, width: Int, height: Int): Ellipse2D? {
        if (oval == null) {
            oval = Ellipse2D.Double(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        } else {
            oval!!.setFrame(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        }
        return oval
    }

    /**
     * Draws an arc contained within the rectangle
     * `(x, y, width, height)`, starting at `startAngle`
     * and continuing through `arcAngle` degrees using
     * the current `paint` and `stroke`.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     * @param startAngle  the start angle in degrees, 0 = 3 o'clock.
     * @param arcAngle  the angle (anticlockwise) in degrees.
     *
     * @see .fillArc
     */
    override fun drawArc(
        x: Int, y: Int, width: Int, height: Int, startAngle: Int,
        arcAngle: Int
    ) {
        Logger.debug { "drawArc($x, $y, $width, $height, $startAngle, $arcAngle)" }
        draw(arc(x, y, width, height, startAngle, arcAngle)!!)
    }

    /**
     * Fills an arc contained within the rectangle
     * `(x, y, width, height)`, starting at `startAngle`
     * and continuing through `arcAngle` degrees, using
     * the current `paint`.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     * @param startAngle  the start angle in degrees, 0 = 3 o'clock.
     * @param arcAngle  the angle (anticlockwise) in degrees.
     *
     * @see .drawArc
     */
    override fun fillArc(
        x: Int, y: Int, width: Int, height: Int, startAngle: Int,
        arcAngle: Int
    ) {
        Logger.debug { "fillArc($x, $y, $width, $height, $startAngle, $arcAngle)" }
        fill(arc(x, y, width, height, startAngle, arcAngle)!!)
    }

    /**
     * Sets the attributes of the reusable [Arc2D] object that is used by
     * [.drawArc] and
     * [.fillArc] methods.
     *
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     * @param startAngle  the start angle in degrees, 0 = 3 o'clock.
     * @param arcAngle  the angle (anticlockwise) in degrees.
     *
     * @return An arc (never `null`).
     */
    private fun arc(
        x: Int, y: Int, width: Int, height: Int, startAngle: Int,
        arcAngle: Int
    ): Arc2D? {
        if (arc == null) {
            arc = Arc2D.Double(
                x.toDouble(),
                y.toDouble(),
                width.toDouble(),
                height.toDouble(),
                startAngle.toDouble(),
                arcAngle.toDouble(),
                Arc2D.OPEN
            )
        } else {
            arc!!.setArc(
                x.toDouble(),
                y.toDouble(),
                width.toDouble(),
                height.toDouble(),
                startAngle.toDouble(),
                arcAngle.toDouble(),
                Arc2D.OPEN
            )
        }
        return arc
    }

    /**
     * Draws the specified multi-segment line using the current
     * `paint` and `stroke`.
     *
     * @param xPoints  the x-points.
     * @param yPoints  the y-points.
     * @param nPoints  the number of points to use for the polyline.
     */
    override fun drawPolyline(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
        Logger.debug { "drawPolyline(int[], int[], int)" }
        val p = createPolygon(xPoints, yPoints, nPoints, false)
        draw(p)
    }

    /**
     * Draws the specified polygon using the current `paint` and
     * `stroke`.
     *
     * @param xPoints  the x-points.
     * @param yPoints  the y-points.
     * @param nPoints  the number of points to use for the polygon.
     *
     * @see .fillPolygon
     */
    override fun drawPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
        Logger.debug { "drawPolygon(int[], int[], int)" }
        val p = createPolygon(xPoints, yPoints, nPoints, true)
        draw(p)
    }

    /**
     * Fills the specified polygon using the current `paint`.
     *
     * @param xPoints  the x-points.
     * @param yPoints  the y-points.
     * @param nPoints  the number of points to use for the polygon.
     *
     * @see .drawPolygon
     */
    override fun fillPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
        Logger.debug { "fillPolygon(int[], int[], int)" }
        val p = createPolygon(xPoints, yPoints, nPoints, true)
        fill(p)
    }

    /**
     * Creates a polygon from the specified `x` and
     * `y` coordinate arrays.
     *
     * @param xPoints  the x-points.
     * @param yPoints  the y-points.
     * @param nPoints  the number of points to use for the polyline.
     * @param close  closed?
     *
     * @return A polygon.
     */
    fun createPolygon(
        xPoints: IntArray, yPoints: IntArray,
        nPoints: Int, close: Boolean
    ): GeneralPath {
        Logger.debug { "createPolygon(int[], int[], int, boolean)" }
        val p = GeneralPath()
        p.moveTo(xPoints[0].toFloat(), yPoints[0].toFloat())
        for (i in 1 until nPoints) {
            p.lineTo(xPoints[i].toFloat(), yPoints[i].toFloat())
        }
        if (close) {
            p.closePath()
        }
        return p
    }

    /**
     * Draws an image at the location `(x, y)`.  Note that the
     * `observer` is ignored.
     *
     * @param img  the image (`null` permitted...method will do nothing).
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param observer  ignored.
     *
     * @return `true` if there is no more drawing to be done.
     */
    override fun drawImage(img: Image?, x: Int, y: Int, observer: ImageObserver?): Boolean {
        Logger.debug { "drawImage(Image, $x, $y, ImageObserver)" }
        if (img == null) {
            return true
        }
        val w = img.getWidth(observer)
        if (w < 0) {
            return false
        }
        val h = img.getHeight(observer)
        return if (h < 0) {
            false
        } else drawImage(img, x, y, w, h, observer)
    }

    /**
     * Draws the image into the rectangle defined by `(x, y, w, h)`.
     * Note that the `observer` is ignored (it is not useful in this
     * context).
     *
     * @param img  the image (`null` permitted...draws nothing).
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param width  the width.
     * @param height  the height.
     * @param observer  ignored.
     *
     * @return `true` if there is no more drawing to be done.
     */
    override fun drawImage(
        img: Image?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        observer: ImageObserver?
    ): Boolean {
        Logger.debug { "drawImage(Image, $x, $y, $width, $height, ImageObserver)" }
        val buffered: BufferedImage
        if (img is BufferedImage) {
            buffered = img
        } else {
            buffered = BufferedImage(
                width, height,
                BufferedImage.TYPE_INT_ARGB
            )
            val g2 = buffered.createGraphics()
            g2.drawImage(img, 0, 0, width, height, null)
            g2.dispose()
        }
        val skijaImage = convertToSkijaImage(buffered)
        canvas!!.drawImageRect(
            skijaImage,
            Rect(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat())
        )
        return true
    }

    /**
     * Draws an image at the location `(x, y)`.  Note that the
     * `observer` is ignored.
     *
     * @param img  the image (`null` permitted...draws nothing).
     * @param x  the x-coordinate.
     * @param y  the y-coordinate.
     * @param bgcolor  the background color (`null` permitted).
     * @param observer  ignored.
     *
     * @return `true` if there is no more drawing to be done.
     */
    override fun drawImage(
        img: Image?, x: Int, y: Int, bgcolor: Color?,
        observer: ImageObserver?
    ): Boolean {
        Logger.debug { "drawImage(Image, $x, $y, Color, ImageObserver)" }
        if (img == null) {
            return true
        }
        val w = img.getWidth(null)
        if (w < 0) {
            return false
        }
        val h = img.getHeight(null)
        return if (h < 0) {
            false
        } else drawImage(img, x, y, w, h, bgcolor, observer)
    }

    override fun drawImage(
        img: Image?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        bgcolor: Color?,
        observer: ImageObserver?
    ): Boolean {
        Logger.debug { "drawImage(Image, $x, $y, $width, $height, Color, ImageObserver)" }
        val saved = paint
        setPaint(bgcolor)
        fillRect(x, y, width, height)
        paint = saved
        return drawImage(img, x, y, width, height, observer)
    }

    /**
     * Draws part of an image (defined by the source rectangle
     * `(sx1, sy1, sx2, sy2)`) into the destination rectangle
     * `(dx1, dy1, dx2, dy2)`.  Note that the `observer`
     * is ignored in this implementation.
     *
     * @param img  the image.
     * @param dx1  the x-coordinate for the top left of the destination.
     * @param dy1  the y-coordinate for the top left of the destination.
     * @param dx2  the x-coordinate for the bottom right of the destination.
     * @param dy2  the y-coordinate for the bottom right of the destination.
     * @param sx1  the x-coordinate for the top left of the source.
     * @param sy1  the y-coordinate for the top left of the source.
     * @param sx2  the x-coordinate for the bottom right of the source.
     * @param sy2  the y-coordinate for the bottom right of the source.
     *
     * @return `true` if the image is drawn.
     */
    override fun drawImage(
        img: Image?,
        dx1: Int,
        dy1: Int,
        dx2: Int,
        dy2: Int,
        sx1: Int,
        sy1: Int,
        sx2: Int,
        sy2: Int,
        observer: ImageObserver?
    ): Boolean {
        Logger.debug { "drawImage(Image, $dx1, $dy1, $dx2, $dy2, $sx1, $sy1, $sx2, $sy2, ImageObserver)" }
        val w = dx2 - dx1
        val h = dy2 - dy1
        val img2 = BufferedImage(
            w, h,
            BufferedImage.TYPE_INT_ARGB
        )
        val g2 = img2.createGraphics()
        g2.drawImage(img, 0, 0, w, h, sx1, sy1, sx2, sy2, null)
        return drawImage(img2, dx1, dy1, null)
    }

    /**
     * Draws part of an image (defined by the source rectangle
     * `(sx1, sy1, sx2, sy2)`) into the destination rectangle
     * `(dx1, dy1, dx2, dy2)`.  The destination rectangle is first
     * cleared by filling it with the specified `bgcolor`. Note that
     * the `observer` is ignored.
     *
     * @param img  the image.
     * @param dx1  the x-coordinate for the top left of the destination.
     * @param dy1  the y-coordinate for the top left of the destination.
     * @param dx2  the x-coordinate for the bottom right of the destination.
     * @param dy2  the y-coordinate for the bottom right of the destination.
     * @param sx1 the x-coordinate for the top left of the source.
     * @param sy1 the y-coordinate for the top left of the source.
     * @param sx2 the x-coordinate for the bottom right of the source.
     * @param sy2 the y-coordinate for the bottom right of the source.
     * @param bgcolor  the background color (`null` permitted).
     * @param observer  ignored.
     *
     * @return `true` if the image is drawn.
     */
    override fun drawImage(
        img: Image?,
        dx1: Int,
        dy1: Int,
        dx2: Int,
        dy2: Int,
        sx1: Int,
        sy1: Int,
        sx2: Int,
        sy2: Int,
        bgcolor: Color?,
        observer: ImageObserver?
    ): Boolean {
        Logger.debug { "drawImage(Image, $dx1, $dy1, $dx2, $dy2, $sx1, $sy1, $sx2, $sy2, Color, ImageObserver)" }
        val saved = paint
        setPaint(bgcolor)
        fillRect(dx1, dy1, dx2 - dx1, dy2 - dy1)
        paint = saved
        return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)
    }

    /**
     * This method does nothing.
     */
    override fun dispose() {
        Logger.debug { "dispose()" }
        canvas!!.restoreToCount(restoreCount)
    }

    companion object {

        /** The line width to use when a BasicStroke with line width = 0.0 is applied.  */
        private const val MIN_LINE_WIDTH = 0.1

        /**
         * Throws an `IllegalArgumentException` if `arg` is
         * `null`.
         *
         * @param arg  the argument to check.
         * @param name  the name of the argument (to display in the exception
         * message).
         */
        private fun nullNotPermitted(arg: Any?, name: String) {
            requireNotNull(arg) { "Null '$name' argument." }
        }

        /**
         * Creates a map containing default mappings from the Java logical font names
         * to suitable physical font names.  This is not a particularly great solution,
         * but so far I don't see a better alternative.
         *
         * @return A map.
         */
        fun createDefaultFontMap(): Map<String, String?> {
            val result: MutableMap<String, String?> = HashMap()
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            if (os.contains("win")) { // Windows
                result[Font.MONOSPACED] = "Courier New"
                result[Font.SANS_SERIF] = "Arial"
                result[Font.SERIF] = "Times New Roman"
            } else if (os.contains("mac")) { // MacOS
                result[Font.MONOSPACED] = "Courier New"
                result[Font.SANS_SERIF] = "Helvetica"
                result[Font.SERIF] = "Times New Roman"
            } else { // assume Linux
                result[Font.MONOSPACED] = "Courier New"
                result[Font.SANS_SERIF] = "Arial"
                result[Font.SERIF] = "Times New Roman"
            }
            result[Font.DIALOG] = result[Font.SANS_SERIF]
            result[Font.DIALOG_INPUT] = result[Font.SANS_SERIF]
            return result
        }

        /**
         * Returns `true` if the two `Paint` objects are equal
         * OR both `null`.  This method handles
         * `GradientPaint`, `LinearGradientPaint`
         * and `RadialGradientPaint` as special cases, since those classes do
         * not override the `equals()` method.
         *
         * @param p1  paint 1 (`null` permitted).
         * @param p2  paint 2 (`null` permitted).
         *
         * @return A boolean.
         */
        private fun paintsAreEqual(p1: Paint?, p2: Paint?): Boolean {
            if (p1 === p2) {
                return true
            }

            // handle cases where either or both arguments are null
            if (p1 == null) {
                return p2 == null
            }
            if (p2 == null) {
                return false
            }

            // handle cases...
            if (p1 is Color && p2 is Color) {
                return p1 == p2
            }
            if (p1 is GradientPaint && p2 is GradientPaint) {
                val gp1 = p1
                val gp2 = p2
                return gp1.color1 == gp2.color1 && gp1.color2 == gp2.color2 && gp1.point1 == gp2.point1 && gp1.point2 == gp2.point2 && gp1.isCyclic == gp2.isCyclic && gp1.transparency == gp1.transparency
            }
            if (p1 is LinearGradientPaint
                && p2 is LinearGradientPaint
            ) {
                val lgp1 = p1
                val lgp2 = p2
                return (lgp1.startPoint == lgp2.startPoint && lgp1.endPoint == lgp2.endPoint && Arrays.equals(
                    lgp1.fractions,
                    lgp2.fractions
                )
                        && Arrays.equals(lgp1.colors, lgp2.colors)
                        && lgp1.cycleMethod == lgp2.cycleMethod && lgp1.colorSpace == lgp2.colorSpace && lgp1.transform == lgp2.transform)
            }
            if (p1 is RadialGradientPaint
                && p2 is RadialGradientPaint
            ) {
                val rgp1 = p1
                val rgp2 = p2
                return (rgp1.centerPoint == rgp2.centerPoint && rgp1.radius == rgp2.radius && rgp1.focusPoint == rgp2.focusPoint && Arrays.equals(
                    rgp1.fractions,
                    rgp2.fractions
                )
                        && Arrays.equals(rgp1.colors, rgp2.colors)
                        && rgp1.cycleMethod == rgp2.cycleMethod && rgp1.colorSpace == rgp2.colorSpace && rgp1.transform == rgp2.transform)
            }
            return p1 == p2
        }

        /**
         * Converts a rendered image to a `BufferedImage`.  This utility
         * method has come from a forum post by Jim Moore at:
         *
         *
         * [
 * http://www.jguru.com/faq/view.jsp?EID=114602](http://www.jguru.com/faq/view.jsp?EID=114602)
         *
         * @param img  the rendered image.
         *
         * @return A buffered image.
         */
        private fun convertRenderedImage(img: RenderedImage): BufferedImage {
            if (img is BufferedImage) {
                return img
            }
            val cm = img.colorModel
            val width = img.width
            val height = img.height
            val raster = cm.createCompatibleWritableRaster(width, height)
            val isAlphaPremultiplied = cm.isAlphaPremultiplied
            val properties: Hashtable<String, Any?> = Hashtable<String, Any?>()
            val keys = img.propertyNames
            if (keys != null) {
                for (key in keys) {
                    properties[key] = img.getProperty(key)
                }
            }
            val result = BufferedImage(
                cm, raster,
                isAlphaPremultiplied, properties
            )
            img.copyData(raster)
            return result
        }

        private fun convertToSkijaImage(image: Image): org.jetbrains.skia.Image {
            val w = image.getWidth(null)
            val h = image.getHeight(null)
            val img = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
            val g2 = img.createGraphics()
            g2.drawImage(image, 0, 0, null)
            val db = img.raster.dataBuffer as DataBufferInt
            val pixels = db.data
            val bytes = ByteArray(pixels.size * 4)
            for (i in pixels.indices) {
                val p = pixels[i]
                bytes[i * 4 + 3] = (p and -0x1000000 shr 24).toByte()
                bytes[i * 4 + 2] = (p and 0xFF0000 shr 16).toByte()
                bytes[i * 4 + 1] = (p and 0xFF00 shr 8).toByte()
                bytes[i * 4] = (p and 0xFF).toByte()
            }
            val imageInfo = ImageInfo(w, h, ColorType.BGRA_8888, ColorAlphaType.PREMUL)
            return org.jetbrains.skia.Image.makeRaster(
                imageInfo,
                bytes,
                image.getWidth(null) * 4
            )
        }
    }
}