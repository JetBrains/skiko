package org.jetbrains.skia

import org.jetbrains.skia.ImageFilter.Companion.makeDropShadowOnly
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

open class Canvas internal constructor(ptr: NativePointer, managed: Boolean, internal val _owner: Any) :
    Managed(ptr, _FinalizerHolder.PTR, managed) {
    companion object {
        init {
            staticLoad()
        }
    }
    /**
     *
     * Constructs a canvas that draws into bitmap.
     * Use props to match the device characteristics, like LCD striping.
     *
     *
     * Bitmap is copied so that subsequently editing bitmap will not affect
     * constructed Canvas.
     *
     * @param bitmap  width, height, ColorType, ColorAlphaType, and pixel
     * storage of raster surface
     * @param surfaceProps   order and orientation of RGB striping; and whether to use
     * device independent fonts
     *
     * @see [https://fiddle.skia.org/c/@Canvas_const_SkBitmap_const_SkSurfaceProps](https://fiddle.skia.org/c/@Canvas_const_SkBitmap_const_SkSurfaceProps)
     */
    /**
     *
     * Constructs a canvas that draws into bitmap.
     * Sets default pixel geometry in constructed Surface.
     *
     *
     * Bitmap is copied so that subsequently editing bitmap will not affect
     * constructed Canvas.
     *
     *
     * May be deprecated in the future.
     *
     * @param bitmap  width, height, ColorType, ColorAlphaType, and pixel
     * storage of raster surface
     *
     * @see [https://fiddle.skia.org/c/@Canvas_copy_const_SkBitmap](https://fiddle.skia.org/c/@Canvas_copy_const_SkBitmap)
     */
    constructor(bitmap: Bitmap, surfaceProps: SurfaceProps = SurfaceProps()) : this(
        _nMakeFromBitmap(
            bitmap._ptr,
            surfaceProps._getFlags(),
            surfaceProps.pixelGeometry.ordinal
        ), true, bitmap
    ) {
        Stats.onNativeCall()
        reachabilityBarrier(bitmap)
    }

    fun drawPoint(x: Float, y: Float, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawPoint(_ptr, x, y, getPtr(paint))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
        return this
    }

    /**
     *
     * Draws pts using clip, Matrix and Paint paint.
     *
     *
     * The shape of point drawn depends on paint
     * PaintStrokeCap. If paint is set to [PaintStrokeCap.ROUND], each point draws a
     * circle of diameter Paint stroke width. If paint is set to [PaintStrokeCap.SQUARE]
     * or [PaintStrokeCap.BUTT], each point draws a square of width and height
     * Paint stroke width.
     *
     *
     * Each line segment respects paint PaintStrokeCap and Paint stroke width.
     * PaintMode is ignored, as if were set to [PaintMode.STROKE].
     *
     *
     * Always draws each element one at a time; is not affected by
     * PaintStrokeJoin, and unlike drawPath(), does not create a mask from all points
     * and lines before drawing.
     *
     * @param coords array of points to draw
     * @param paint  stroke, blend, color, and so on, used to draw
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawPoints](https://fiddle.skia.org/c/@Canvas_drawPoints)
     */
    fun drawPoints(coords: Array<Point>, paint: Paint): Canvas {
        return drawPoints(Point.flattenArray(coords)!!, paint)
    }

    /**
     *
     * Draws pts using clip, Matrix and Paint paint.
     *
     *
     * The shape of point drawn depends on paint
     * PaintStrokeCap. If paint is set to [PaintStrokeCap.ROUND], each point draws a
     * circle of diameter Paint stroke width. If paint is set to [PaintStrokeCap.SQUARE]
     * or [PaintStrokeCap.BUTT], each point draws a square of width and height
     * Paint stroke width.
     *
     *
     * Each line segment respects paint PaintStrokeCap and Paint stroke width.
     * PaintMode is ignored, as if were set to [PaintMode.STROKE].
     *
     *
     * Always draws each element one at a time; is not affected by
     * PaintStrokeJoin, and unlike drawPath(), does not create a mask from all points
     * and lines before drawing.
     *
     * @param coords array of points to draw
     * @param paint  stroke, blend, color, and so on, used to draw
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawPoints](https://fiddle.skia.org/c/@Canvas_drawPoints)
     */
    fun drawPoints(coords: FloatArray, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawPoints(_ptr, 0 /* SkCanvas::PointMode::kPoints_PointMode */, coords.size, toInterop(coords), getPtr(paint))
            }
        } finally {
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    /**
     *
     * Draws pts using clip, Matrix and Paint paint.
     *
     *
     * Each pair of points draws a line segment.
     * One line is drawn for every two points; each point is used once. If count is odd,
     * the final point is ignored.
     *
     *
     * Each line segment respects paint PaintStrokeCap and Paint stroke width.
     * PaintMode is ignored, as if were set to [PaintMode.STROKE].
     *
     *
     * Always draws each element one at a time; is not affected by
     * PaintStrokeJoin, and unlike drawPath(), does not create a mask from all points
     * and lines before drawing.
     *
     * @param coords array of points to draw
     * @param paint  stroke, blend, color, and so on, used to draw
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawPoints](https://fiddle.skia.org/c/@Canvas_drawPoints)
     */
    fun drawLines(coords: Array<Point>, paint: Paint): Canvas {
        return drawLines(Point.flattenArray(coords)!!, paint)
    }

    /**
     *
     * Draws pts using clip, Matrix and Paint paint.
     *
     *
     * Each pair of points draws a line segment.
     * One line is drawn for every two points; each point is used once. If count is odd,
     * the final point is ignored.
     *
     *
     * Each line segment respects paint PaintStrokeCap and Paint stroke width.
     * PaintMode is ignored, as if were set to [PaintMode.STROKE].
     *
     *
     * Always draws each element one at a time; is not affected by
     * PaintStrokeJoin, and unlike drawPath(), does not create a mask from all points
     * and lines before drawing.
     *
     * @param coords array of points to draw
     * @param paint  stroke, blend, color, and so on, used to draw
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawPoints](https://fiddle.skia.org/c/@Canvas_drawPoints)
     */
    fun drawLines(coords: FloatArray, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawPoints(_ptr, 1 /* SkCanvas::PointMode::kLines_PointMode */, coords.size, toInterop(coords),getPtr(paint))
            }
        } finally {
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    /**
     *
     * Draws pts using clip, Matrix and Paint paint.
     *
     *
     * Each adjacent pair of points draws a line segment.
     * count minus one lines are drawn; the first and last point are used once.
     *
     *
     * Each line segment respects paint PaintStrokeCap and Paint stroke width.
     * PaintMode is ignored, as if were set to [PaintMode.STROKE].
     *
     *
     * Always draws each element one at a time; is not affected by
     * PaintStrokeJoin, and unlike drawPath(), does not create a mask from all points
     * and lines before drawing.
     *
     * @param coords array of points to draw
     * @param paint  stroke, blend, color, and so on, used to draw
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawPoints](https://fiddle.skia.org/c/@Canvas_drawPoints)
     */
    fun drawPolygon(coords: Array<Point>, paint: Paint): Canvas {
        return drawPolygon(Point.flattenArray(coords)!!, paint)
    }

    /**
     *
     * Draws pts using clip, Matrix and Paint paint.
     *
     *
     * Each adjacent pair of points draws a line segment.
     * count minus one lines are drawn; the first and last point are used once.
     *
     *
     * Each line segment respects paint PaintStrokeCap and Paint stroke width.
     * PaintMode is ignored, as if were set to [PaintMode.STROKE].
     *
     *
     * Always draws each element one at a time; is not affected by
     * PaintStrokeJoin, and unlike drawPath(), does not create a mask from all points
     * and lines before drawing.
     *
     * @param coords array of points to draw
     * @param paint  stroke, blend, color, and so on, used to draw
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawPoints](https://fiddle.skia.org/c/@Canvas_drawPoints)
     */
    fun drawPolygon(coords: FloatArray, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawPoints(_ptr, 2 /* SkCanvas::PointMode::kPolygon_PointMode */, coords.size, toInterop(coords), getPtr(paint))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
        return this
    }

    fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawLine(_ptr, x0, y0, x1, y1, getPtr(paint))
        } finally {
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    fun drawArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        includeCenter: Boolean,
        paint: Paint
    ): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawArc(_ptr, left, top, right, bottom, startAngle, sweepAngle, includeCenter, getPtr(paint))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
        return this
    }

    fun drawRect(r: Rect, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawRect(_ptr, r.left, r.top, r.right, r.bottom, getPtr(paint))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
        return this
    }

    fun drawOval(r: Rect, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawOval(_ptr, r.left, r.top, r.right, r.bottom, getPtr(paint))
        } finally {
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    fun drawCircle(x: Float, y: Float, radius: Float, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawOval(_ptr, x - radius, y - radius, x + radius, y + radius, getPtr(paint))
        } finally {
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    fun drawRRect(r: RRect, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawRRect(_ptr, r.left, r.top, r.right, r.bottom, toInterop(r.radii), r.radii.size, getPtr(paint))
            }
        } finally {
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    fun drawDRRect(outer: RRect, inner: RRect, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawDRRect(
                    _ptr,
                    outer.left,
                    outer.top,
                    outer.right,
                    outer.bottom,
                    toInterop(outer.radii),
                    outer.radii.size,
                    inner.left,
                    inner.top,
                    inner.right,
                    inner.bottom,
                    toInterop(inner.radii),
                    inner.radii.size,
                    getPtr(paint)
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
        return this
    }

    fun drawRectShadow(r: Rect, dx: Float, dy: Float, blur: Float, color: Int): Canvas {
        return drawRectShadow(r, dx, dy, blur, 0f, color)
    }

    fun drawRectShadow(r: Rect, dx: Float, dy: Float, blur: Float, spread: Float, color: Int): Canvas {
        val insides = r.inflate(-1f)
        if (!insides.isEmpty) {
            save()
            if (insides is RRect) clipRRect(insides, ClipMode.DIFFERENCE) else clipRect(insides, ClipMode.DIFFERENCE)
            drawRectShadowNoclip(r, dx, dy, blur, spread, color)
            restore()
        } else drawRectShadowNoclip(r, dx, dy, blur, spread, color)
        return this
    }

    fun drawRectShadowNoclip(r: Rect, dx: Float, dy: Float, blur: Float, spread: Float, color: Int): Canvas {
        val outline = r.inflate(spread)
        makeDropShadowOnly(dx, dy, blur / 2f, blur / 2f, color).use { f ->
            org.jetbrains.skia.Paint().use { p ->
                p.imageFilter = f
                if (outline is RRect) drawRRect(outline, p) else drawRect(outline, p)
            }
        }
        return this
    }

    fun drawPath(path: Path, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawPath(_ptr, getPtr(path), getPtr(paint))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(path)
            reachabilityBarrier(paint)
        }
        return this
    }

    fun drawImage(image: Image, left: Float, top: Float): Canvas {
        return drawImageRect(
            image,
            Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
            Rect.makeXYWH(left, top, image.width.toFloat(), image.height.toFloat()),
            SamplingMode.DEFAULT,
            null,
            true
        )
    }

    fun drawImage(image: Image, left: Float, top: Float, paint: Paint?): Canvas {
        return drawImageRect(
            image,
            Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
            Rect.makeXYWH(left, top, image.width.toFloat(), image.height.toFloat()),
            SamplingMode.DEFAULT,
            paint,
            true
        )
    }

    fun drawImageRect(image: Image, dst: Rect): Canvas {
        return drawImageRect(
            image,
            Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
            dst,
            SamplingMode.DEFAULT,
            null,
            true
        )
    }

    fun drawImageRect(image: Image, dst: Rect, paint: Paint?): Canvas {
        return drawImageRect(
            image,
            Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
            dst,
            SamplingMode.DEFAULT,
            paint,
            true
        )
    }

    fun drawImageRect(image: Image, src: Rect, dst: Rect): Canvas {
        return drawImageRect(image, src, dst, SamplingMode.DEFAULT, null, true)
    }

    fun drawImageRect(image: Image, src: Rect, dst: Rect, paint: Paint?): Canvas {
        return drawImageRect(image, src, dst, SamplingMode.DEFAULT, paint, true)
    }

    fun drawImageRect(image: Image, src: Rect, dst: Rect, paint: Paint?, strict: Boolean): Canvas {
        return drawImageRect(image, src, dst, SamplingMode.DEFAULT, paint, strict)
    }

    fun drawImageRect(
        image: Image,
        src: Rect,
        dst: Rect,
        samplingMode: SamplingMode,
        paint: Paint?,
        strict: Boolean
    ): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawImageRect(
                _ptr,
                getPtr(image),
                src.left,
                src.top,
                src.right,
                src.bottom,
                dst.left,
                dst.top,
                dst.right,
                dst.bottom,
                samplingMode._packedInt1(),
                samplingMode._packedInt2(),
                getPtr(paint),
                strict
            )
        } finally {
            reachabilityBarrier(image)
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    fun drawImageNine(image: Image, center: IRect, dst: Rect, filterMode: FilterMode, paint: Paint?): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawImageNine(
                _ptr,
                getPtr(image),
                center.left,
                center.top,
                center.right,
                center.bottom,
                dst.left,
                dst.top,
                dst.right,
                dst.bottom,
                filterMode.ordinal,
                getPtr(paint)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(image)
            reachabilityBarrier(paint)
        }
        return this
    }

    fun drawRegion(r: Region, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawRegion(_ptr, getPtr(r), getPtr(paint))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(r)
            reachabilityBarrier(paint)
        }
        return this
    }

    fun drawString(s: String, x: Float, y: Float, font: Font?, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawString(_ptr, toInterop(s), x, y, getPtr(font), getPtr(paint))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(font)
            reachabilityBarrier(paint)
        }
        return this
    }

    /**
     * Draw a text [blob] with baseline starting at [x] [y] with [paint]
     */
    fun drawTextBlob(blob: TextBlob, x: Float, y: Float, paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawTextBlob(_ptr, getPtr(blob), x, y, getPtr(paint))
        } finally {
            reachabilityBarrier(blob)
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    /**
     * Draw a text [line] with baseline starting at [x] [y] with [paint]
     */
    fun drawTextLine(line: TextLine, x: Float, y: Float, paint: Paint): Canvas {
        line.textBlob?.use { blob -> blob.let { drawTextBlob(it, x, y, paint) } }
        return this
    }

    fun drawPicture(picture: Picture, matrix: Matrix33? = null, paint: Paint? = null): Canvas {
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawPicture(_ptr, getPtr(picture), toInterop(matrix?.mat), getPtr(paint))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(picture)
            reachabilityBarrier(paint)
        }
        return this
    }

    /**
     *
     * Draws a triangle mesh, using clip and Matrix.
     *
     *
     * If paint contains an Shader and vertices does not contain texCoords, the shader
     * is mapped using the vertices' positions.
     *
     *
     * If vertices colors are defined in vertices, and Paint paint contains Shader,
     * BlendMode mode combines vertices colors with Shader.
     *
     * @param positions  triangle mesh to draw
     * @param colors     color array, one for each corner; may be null
     * @param texCoords  Point array of texture coordinates, mapping Shader to corners; may be null
     * @param indices    with which indices points should be drawn; may be null
     * @param blendMode  combines vertices colors with Shader, if both are present
     * @param paint      specifies the Shader, used as Vertices texture
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawVertices](https://fiddle.skia.org/c/@Canvas_drawVertices)
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawVertices_2](https://fiddle.skia.org/c/@Canvas_drawVertices_2)
     */
    fun drawTriangles(
        positions: Array<Point>,
        colors: IntArray? = null,
        texCoords: Array<Point>? = null,
        indices: ShortArray? = null,
        blendMode: BlendMode,
        paint: Paint
    ): Canvas {
        require(positions.size % 3 == 0) { "Expected positions.length % 3 == 0, got: " + positions.size }
        require(colors == null || colors.size == positions.size) { "Expected colors.length == positions.length, got: " + colors!!.size + " != " + positions.size }
        require(texCoords == null || texCoords.size == positions.size) { "Expected texCoords.length == positions.length, got: " + texCoords!!.size + " != " + positions.size }
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawVertices(
                    _ptr,
                    0 /* kTriangles_VertexMode */,
                    positions.size,
                    toInterop(Point.flattenArray(positions)),
                    toInterop(colors),
                    toInterop(Point.flattenArray(texCoords)),
                    indices?.size ?: 0,
                    toInterop(indices),
                    blendMode.ordinal,
                    getPtr(paint)
                )
            }
        } finally {
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    /**
     *
     * Draws a triangle strip mesh, using clip and Matrix.
     *
     *
     * If paint contains an Shader and vertices does not contain texCoords, the shader
     * is mapped using the vertices' positions.
     *
     *
     * If vertices colors are defined in vertices, and Paint paint contains Shader,
     * BlendMode mode combines vertices colors with Shader.
     *
     * @param positions  triangle mesh to draw
     * @param colors     color array, one for each corner; may be null
     * @param texCoords  Point array of texture coordinates, mapping Shader to corners; may be null
     * @param indices    with which indices points should be drawn; may be null
     * @param blendMode  combines vertices colors with Shader, if both are present
     * @param paint      specifies the Shader, used as Vertices texture
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawVertices](https://fiddle.skia.org/c/@Canvas_drawVertices)
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawVertices_2](https://fiddle.skia.org/c/@Canvas_drawVertices_2)
     */
    fun drawTriangleStrip(
        positions: Array<Point>,
        colors: IntArray? = null,
        texCoords: Array<Point>? = null,
        indices: ShortArray? = null,
        blendMode: BlendMode,
        paint: Paint
    ): Canvas {
        require(colors == null || colors.size == positions.size) { "Expected colors.length == positions.length, got: " + colors!!.size + " != " + positions.size }
        require(texCoords == null || texCoords.size == positions.size) { "Expected texCoords.length == positions.length, got: " + texCoords!!.size + " != " + positions.size }
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawVertices(
                    _ptr,
                    1 /* kTriangleStrip_VertexMode */,
                    positions.size,
                    toInterop(Point.flattenArray(positions)),
                    toInterop(colors),
                    toInterop(Point.flattenArray(texCoords)),
                    indices?.size ?: 0,
                    toInterop(indices),
                    blendMode.ordinal,
                    getPtr(paint)
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
        return this
    }

    /**
     *
     * Draws a triangle fan mesh, using clip and Matrix.
     *
     *
     * If paint contains an Shader and vertices does not contain texCoords, the shader
     * is mapped using the vertices' positions.
     *
     *
     * If vertices colors are defined in vertices, and Paint paint contains Shader,
     * BlendMode mode combines vertices colors with Shader.
     *
     * @param positions  triangle mesh to draw
     * @param colors     color array, one for each corner; may be null
     * @param texCoords  Point array of texture coordinates, mapping Shader to corners; may be null
     * @param indices    with which indices points should be drawn; may be null
     * @param blendMode  combines vertices colors with Shader, if both are present
     * @param paint      specifies the Shader, used as Vertices texture
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawVertices](https://fiddle.skia.org/c/@Canvas_drawVertices)
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawVertices_2](https://fiddle.skia.org/c/@Canvas_drawVertices_2)
     */
    fun drawTriangleFan(
        positions: Array<Point>,
        colors: IntArray? = null,
        texCoords: Array<Point>? = null,
        indices: ShortArray? = null,
        blendMode: BlendMode,
        paint: Paint
    ): Canvas {
        require(colors == null || colors.size == positions.size) { "Expected colors.length == positions.length, got: " + colors!!.size + " != " + positions.size }
        require(texCoords == null || texCoords.size == positions.size) { "Expected texCoords.length == positions.length, got: " + texCoords!!.size + " != " + positions.size }
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawVertices(
                    _ptr,
                    2 /* kTriangleFan_VertexMode */,
                    positions.size,
                    toInterop(Point.flattenArray(positions)),
                    toInterop(colors),
                    toInterop(Point.flattenArray(texCoords)),
                    indices?.size ?: 0,
                    toInterop(indices),
                    blendMode.ordinal,
                    getPtr(paint)
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
        return this
    }

    /**
     *
     * Draws a triangle, triangle strip or triangle fan mesh, using clip and Matrix.
     *
     *
     * If paint contains an Shader and vertices does not contain texCoords, the shader
     * is mapped using the vertices' positions.
     *
     *
     * If vertices colors are defined in vertices, and Paint paint contains Shader,
     * BlendMode mode combines vertices colors with Shader.
     *
     * @param vertexMode 0 - triangles, 1 - triangle strip, 2 - triangle fan
     * @param positions  flattened array of the mesh to draw
     * @param colors     color array, one for each corner; may be null
     * @param texCoords  flattened Point array of texture coordinates, mapping Shader to corners; may be null
     * @param indices    with which indices points should be drawn; may be null
     * @param blendMode  combines vertices colors with Shader, if both are present
     * @param paint      specifies the Shader, used as Vertices texture
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawVertices](https://fiddle.skia.org/c/@Canvas_drawVertices)
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawVertices_2](https://fiddle.skia.org/c/@Canvas_drawVertices_2)
     */

    fun drawVertices(
        vertexMode: VertexMode,
        positions: FloatArray,
        colors: IntArray? = null,
        texCoords: FloatArray? = null,
        indices: ShortArray? = null,
        blendMode: BlendMode,
        paint: Paint
    ): Canvas {
        require(positions.size % 2 == 0) {
            "Expected even number of positions: ${positions.size}"
        }
        val points = positions.size / 2
        require(colors == null || colors.size == points) {
            "Expected colors.length == positions.length / 2, got: " + colors!!.size + " != " + points
        }
        require(texCoords == null || texCoords.size == positions.size) {
            "Expected texCoords.length == positions.length, got: " + texCoords!!.size + " != " + positions.size
        }
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawVertices(
                    _ptr,
                    vertexMode.ordinal,
                    points,
                    toInterop(positions),
                    toInterop(colors),
                    toInterop(texCoords),
                    indices?.size ?: 0,
                    toInterop(indices),
                    blendMode.ordinal,
                    getPtr(paint)
                )
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
        return this
    }

    /**
     *
     * Draws a Coons patch: the interpolation of four cubics with shared corners,
     * associating a color, and optionally a texture SkPoint, with each corner.
     *
     *
     * Coons patch uses clip and Matrix, paint Shader, ColorFilter,
     * alpha, ImageFilter, and BlendMode. If Shader is provided it is treated
     * as Coons patch texture; BlendMode mode combines color colors and Shader if
     * both are provided.
     *
     *
     * Point array cubics specifies four Path cubic starting at the top-left corner,
     * in clockwise order, sharing every fourth point. The last Path cubic ends at the
     * first point.
     *
     *
     * Color array color associates colors with corners in top-left, top-right,
     * bottom-right, bottom-left order.C
     *
     *
     * If paint contains Shader, Point array texCoords maps Shader as texture to
     * corners in top-left, top-right, bottom-right, bottom-left order. If texCoords is
     * nullptr, Shader is mapped using positions (derived from cubics).
     *
     * @param cubics     Path cubic array, sharing common points
     * @param colors     color array, one for each corner
     * @param texCoords  Point array of texture coordinates, mapping Shader to corners;
     * may be null
     * @param blendMode  BlendMode for colors, and for Shader if paint has one
     * @param paint      Shader, ColorFilter, BlendMode, used to draw
     * @return           this
     *
     * @see [https://fiddle.skia.org/c/4cf70f8d194867d053d7e177e5088445](https://fiddle.skia.org/c/4cf70f8d194867d053d7e177e5088445)
     */
    fun drawPatch(
        cubics: Array<Point>,
        colors: IntArray,
        texCoords: Array<Point>? = null,
        blendMode: BlendMode,
        paint: Paint
    ): Canvas {
        require(cubics.size == 12) { "Expected cubics.length == 12, got: " + cubics.size }
        require(colors.size == 4) { "Expected colors.length == 4, got: " + colors.size }
        require(texCoords == null || texCoords.size == 4) { "Expected texCoords.length == 4, got: " + texCoords!!.size }
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawPatch(
                    _ptr,
                    toInterop(Point.flattenArray(cubics)),
                    toInterop(colors),
                    toInterop(Point.flattenArray(texCoords)),
                    blendMode.ordinal,
                    getPtr(paint)
                )
            }
        } finally {
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    /**
     *
     * Draws Drawable drawable using clip and matrix.
     *
     *
     * If Canvas has an asynchronous implementation, as is the case
     * when it is recording into Picture, then drawable will be referenced,
     * so that Drawable::draw() can be called when the operation is finalized. To force
     * immediate drawing, call Drawable::draw() instead.
     *
     * @param drawable  custom struct encapsulating drawing commands
     * @return          this
     */
    fun drawDrawable(drawable: Drawable): Canvas {
        return drawDrawable(drawable, null)
    }

    /**
     *
     * Draws Drawable drawable using clip and matrix, offset by (x, y).
     *
     *
     * If Canvas has an asynchronous implementation, as is the case
     * when it is recording into Picture, then drawable will be referenced,
     * so that Drawable::draw() can be called when the operation is finalized. To force
     * immediate drawing, call Drawable::draw() instead.
     *
     * @param drawable  custom struct encapsulating drawing commands
     * @param x         offset into Canvas writable pixels on x-axis
     * @param y         offset into Canvas writable pixels on y-axis
     * @return          this
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawDrawable_2](https://fiddle.skia.org/c/@Canvas_drawDrawable_2)
     */
    fun drawDrawable(drawable: Drawable, x: Float, y: Float): Canvas {
        return drawDrawable(drawable, Matrix33.makeTranslate(x, y))
    }

    /**
     *
     * Draws Drawable drawable using clip and matrix, concatenated with
     * optional matrix.
     *
     *
     * If Canvas has an asynchronous implementation, as is the case
     * when it is recording into Picture, then drawable will be referenced,
     * so that Drawable::draw() can be called when the operation is finalized. To force
     * immediate drawing, call Drawable::draw() instead.
     *
     * @param drawable  custom struct encapsulating drawing commands
     * @param matrix    transformation applied to drawing; may be null
     * @return          this
     *
     * @see [https://fiddle.skia.org/c/@Canvas_drawDrawable](https://fiddle.skia.org/c/@Canvas_drawDrawable)
     */
    fun drawDrawable(drawable: Drawable, matrix: Matrix33?): Canvas {
        Stats.onNativeCall()
        try {
            interopScope {
                _nDrawDrawable(_ptr, getPtr(drawable), toInterop(matrix?.mat))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(drawable)
        }
        return this
    }

    fun clear(color: Int): Canvas {
        Stats.onNativeCall()
        try {
            _nClear(_ptr, color)
        } finally {
            reachabilityBarrier(this)
        }
        return this
    }

    fun drawPaint(paint: Paint): Canvas {
        Stats.onNativeCall()
        try {
            _nDrawPaint(_ptr, getPtr(paint))
        } finally {
            reachabilityBarrier(paint)
            reachabilityBarrier(this)
        }
        return this
    }

    /**
     * Replaces Matrix with matrix.
     * Unlike concat(), any prior matrix state is overwritten.
     *
     * @param matrix  matrix to copy, replacing existing Matrix
     *
     * @see [https://fiddle.skia.org/c/@Canvas_setMatrix](https://fiddle.skia.org/c/@Canvas_setMatrix)
     */
    fun setMatrix(matrix: Matrix33): Canvas {
        Stats.onNativeCall()
        interopScope {
            _nSetMatrix(_ptr, toInterop(matrix.mat))
        }
        return this
    }

    /**
     * Sets SkMatrix to the identity matrix.
     * Any prior matrix state is overwritten.
     *
     * @see [https://fiddle.skia.org/c/@Canvas_resetMatrix](https://fiddle.skia.org/c/@Canvas_resetMatrix)
     */
    fun resetMatrix(): Canvas {
        Stats.onNativeCall()
        _nResetMatrix(_ptr)
        return this
    }

    /**
     * Returns the total transformation matrix for the canvas.
     */
    val localToDevice: Matrix44
        get() = try {
            Stats.onNativeCall()
            Matrix44.fromInteropPointer { interopPointer -> _nGetLocalToDevice(_ptr, interopPointer) }
        } finally {
            reachabilityBarrier(this)
        }

    val localToDeviceAsMatrix33: Matrix33
        get() = localToDevice.asMatrix33()

    fun clipRect(r: Rect, mode: ClipMode, antiAlias: Boolean): Canvas {
        Stats.onNativeCall()
        _nClipRect(_ptr, r.left, r.top, r.right, r.bottom, mode.ordinal, antiAlias)
        return this
    }

    fun clipRect(r: Rect, mode: ClipMode): Canvas {
        return clipRect(r, mode, false)
    }

    fun clipRect(r: Rect, antiAlias: Boolean): Canvas {
        return clipRect(r, ClipMode.INTERSECT, antiAlias)
    }

    fun clipRect(r: Rect): Canvas {
        return clipRect(r, ClipMode.INTERSECT, false)
    }

    fun clipRRect(r: RRect, mode: ClipMode, antiAlias: Boolean): Canvas {
        Stats.onNativeCall()
        interopScope {
            _nClipRRect(_ptr, r.left, r.top, r.right, r.bottom, toInterop(r.radii), r.radii.size, mode.ordinal, antiAlias)
        }
        return this
    }

    fun clipRRect(r: RRect, mode: ClipMode): Canvas {
        return clipRRect(r, mode, false)
    }

    fun clipRRect(r: RRect, antiAlias: Boolean): Canvas {
        return clipRRect(r, ClipMode.INTERSECT, antiAlias)
    }

    fun clipRRect(r: RRect): Canvas {
        return clipRRect(r, ClipMode.INTERSECT, false)
    }

    fun clipPath(p: Path, mode: ClipMode, antiAlias: Boolean): Canvas {
        Stats.onNativeCall()
        try {
            _nClipPath(_ptr, getPtr(p), mode.ordinal, antiAlias)
        } finally {
            reachabilityBarrier(p)
            reachabilityBarrier(this)
        }
        return this
    }

    fun clipPath(p: Path, mode: ClipMode): Canvas {
        return clipPath(p, mode, false)
    }

    fun clipPath(p: Path, antiAlias: Boolean): Canvas {
        return clipPath(p, ClipMode.INTERSECT, antiAlias)
    }

    fun clipPath(p: Path): Canvas {
        return clipPath(p, ClipMode.INTERSECT, false)
    }

    fun clipRegion(r: Region, mode: ClipMode): Canvas {
        Stats.onNativeCall()
        try {
            _nClipRegion(_ptr, getPtr(r), mode.ordinal)
        } finally {
            reachabilityBarrier(r)
            reachabilityBarrier(this)
        }
        return this
    }

    fun clipRegion(r: Region): Canvas {
        return clipRegion(r, ClipMode.INTERSECT)
    }

    fun translate(dx: Float, dy: Float): Canvas {
        interopScope {
            Stats.onNativeCall()
            _nTranslate(_ptr, dx, dy)
        }
        return this
    }

    fun scale(sx: Float, sy: Float): Canvas {
        interopScope {
            Stats.onNativeCall()
            _nScale(_ptr, sx, sy)
        }
        return this
    }

    /**
     * @param deg  angle in degrees
     * @return     this
     */
    fun rotate(deg: Float): Canvas {
        interopScope {
            Stats.onNativeCall()
            _nRotate(_ptr, deg, 0f, 0f)
        }
        return this
    }

    fun rotate(deg: Float, x: Float, y: Float): Canvas {
        interopScope {
            Stats.onNativeCall()
            _nRotate(_ptr, deg, x, y)
        }
        return this
    }

    fun skew(sx: Float, sy: Float): Canvas {
        interopScope {
            Stats.onNativeCall()
            _nSkew(_ptr, sx, sy)
        }
        return this
    }

    fun concat(matrix: Matrix33): Canvas {
        interopScope {
            Stats.onNativeCall()
            _nConcat(
                _ptr, toInterop(matrix.mat)
            )
        }
        return this
    }

    fun concat(matrix: Matrix44): Canvas {
        interopScope {
            Stats.onNativeCall()
            _nConcat44(
                _ptr, toInterop(matrix.mat)
            )
        }
        return this
    }

    /**
     *
     * Copies Rect of pixels from Canvas into bitmap. Matrix and clip are
     * ignored.
     *
     *
     * Source Rect corners are (srcX, srcY) and (imageInfo().width(), imageInfo().height()).
     * Destination Rect corners are (0, 0) and (bitmap.width(), bitmap.height()).
     * Copies each readable pixel intersecting both rectangles, without scaling,
     * converting to bitmap.colorType() and bitmap.alphaType() if required.
     *
     *
     * Pixels are readable when BaseDevice is raster, or backed by a GPU.
     * Pixels are not readable when Canvas is returned by Document::beginPage,
     * returned by PictureRecorder::beginRecording, or Canvas is the base of a utility
     * class like DebugCanvas.
     *
     *
     * Caller must allocate pixel storage in bitmap if needed.
     *
     *
     * SkBitmap values are converted only if ColorType and AlphaType
     * do not match. Only pixels within both source and destination rectangles
     * are copied. Bitmap pixels outside Rect intersection are unchanged.
     *
     *
     * Pass negative values for srcX or srcY to offset pixels across or down bitmap.
     *
     *
     * Does not copy, and returns false if:
     *
     *  * Source and destination rectangles do not intersect.
     *  * SkCanvas pixels could not be converted to bitmap.colorType() or bitmap.alphaType().
     *  * SkCanvas pixels are not readable; for instance, Canvas is document-based.
     *  * bitmap pixels could not be allocated.
     *  * bitmap.rowBytes() is too small to contain one row of pixels.
     *
     *
     * @param bitmap  storage for pixels copied from Canvas
     * @param srcX    offset into readable pixels on x-axis; may be negative
     * @param srcY    offset into readable pixels on y-axis; may be negative
     * @return        true if pixels were copied
     *
     * @see [https://fiddle.skia.org/c/@Canvas_readPixels_3](https://fiddle.skia.org/c/@Canvas_readPixels_3)
     */
    fun readPixels(bitmap: Bitmap, srcX: Int, srcY: Int): Boolean {
        return try {
            Stats.onNativeCall()
            _nReadPixels(
                _ptr,
                getPtr(bitmap),
                srcX,
                srcY
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(bitmap)
        }
    }

    /**
     *
     * Copies Rect from pixels to Canvas. Matrix and clip are ignored.
     * Source Rect corners are (0, 0) and (bitmap.width(), bitmap.height()).
     *
     *
     * Destination Rect corners are (x, y) and
     * (imageInfo().width(), imageInfo().height()).
     *
     *
     * Copies each readable pixel intersecting both rectangles, without scaling,
     * converting to getImageInfo().getColorType() and getImageInfo().getAlphaType() if required.
     *
     *
     * Pixels are writable when BaseDevice is raster, or backed by a GPU.
     * Pixels are not writable when Canvas is returned by Document::beginPage,
     * returned by PictureRecorder::beginRecording, or Canvas is the base of a utility
     * class like DebugCanvas.
     *
     *
     * Pixel values are converted only if ColorType and AlphaType
     * do not match. Only pixels within both source and destination rectangles
     * are copied. Canvas pixels outside Rect intersection are unchanged.
     *
     *
     * Pass negative values for x or y to offset pixels to the left or
     * above Canvas pixels.
     *
     *
     * Does not copy, and returns false if:
     *
     *  * Source and destination rectangles do not intersect.
     *  * bitmap does not have allocated pixels.
     *  * bitmap pixels could not be converted to Canvas getImageInfo().getColorType() or
     * getImageInfo().getAlphaType().
     *  * Canvas pixels are not writable; for instance, Canvas is document based.
     *  * bitmap pixels are inaccessible; for instance, bitmap wraps a texture.
     *
     *
     * @param bitmap  contains pixels copied to Canvas
     * @param x       offset into Canvas writable pixels on x-axis; may be negative
     * @param y       offset into Canvas writable pixels on y-axis; may be negative
     * @return        true if pixels were written to Canvas
     *
     * @see [https://fiddle.skia.org/c/@Canvas_writePixels_2](https://fiddle.skia.org/c/@Canvas_writePixels_2)
     *
     * @see [https://fiddle.skia.org/c/@State_Stack_a](https://fiddle.skia.org/c/@State_Stack_a)
     *
     * @see [https://fiddle.skia.org/c/@State_Stack_b](https://fiddle.skia.org/c/@State_Stack_b)
     */
    fun writePixels(bitmap: Bitmap, x: Int, y: Int): Boolean {
        return try {
            Stats.onNativeCall()
            _nWritePixels(
                _ptr,
                getPtr(bitmap),
                x,
                y
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(bitmap)
        }
    }

    fun save(): Int {
        return try {
            Stats.onNativeCall()
            _nSave(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun saveLayer(left: Float, top: Float, right: Float, bottom: Float, paint: Paint?): Int {
        return try {
            Stats.onNativeCall()
            _nSaveLayerRect(
                _ptr,
                left,
                top,
                right,
                bottom,
                getPtr(paint)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
    }

    /**
     *
     * Saves Matrix and clip, and allocates a Bitmap for subsequent drawing.
     * Calling restore() discards changes to Matrix and clip, and draws the Bitmap.
     *
     *
     * Matrix may be changed by translate(), scale(), rotate(), skew(), concat(),
     * setMatrix(), and resetMatrix(). Clip may be changed by clipRect(), clipRRect(),
     * clipPath(), clipRegion().
     *
     *
     * Rect bounds suggests but does not define the Bitmap size. To clip drawing to
     * a specific rectangle, use clipRect().
     *
     *
     * Optional Paint paint applies alpha, ColorFilter, ImageFilter, and
     * BlendMode when restore() is called.
     *
     *
     * Call restoreToCount() with returned value to restore this and subsequent saves.
     *
     * @param bounds  hint to limit the size of the layer
     * @param paint   graphics state for layer; may be null
     * @return        depth of saved stack
     *
     * @see [https://fiddle.skia.org/c/@Canvas_saveLayer](https://fiddle.skia.org/c/@Canvas_saveLayer)
     *
     * @see [https://fiddle.skia.org/c/@Canvas_saveLayer_4](https://fiddle.skia.org/c/@Canvas_saveLayer_4)
     */
    fun saveLayer(bounds: Rect?, paint: Paint?): Int {
        return try {
            Stats.onNativeCall()
            if (bounds == null) _nSaveLayer(
                _ptr,
                getPtr(paint)
            ) else _nSaveLayerRect(
                _ptr,
                bounds.left,
                bounds.top,
                bounds.right,
                bounds.bottom,
                getPtr(paint)
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
    }

    val saveCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetSaveCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun restore(): Canvas {
        Stats.onNativeCall()
        _nRestore(_ptr)
        return this
    }

    fun restoreToCount(saveCount: Int): Canvas {
        Stats.onNativeCall()
        _nRestoreToCount(_ptr, saveCount)
        return this
    }

    private object _FinalizerHolder {
        val PTR = Canvas_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nGetFinalizer")
private external fun Canvas_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nMakeFromBitmap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nMakeFromBitmap")
private external fun _nMakeFromBitmap(bitmapPtr: NativePointer, flags: Int, pixelGeometry: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPoint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawPoint")
private external fun _nDrawPoint(ptr: NativePointer, x: Float, y: Float, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPoints")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawPoints")
private external fun _nDrawPoints(ptr: NativePointer, mode: Int, coordsCount: Int, coords: InteropPointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawLine")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawLine")
private external fun _nDrawLine(ptr: NativePointer, x0: Float, y0: Float, x1: Float, y1: Float, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawArc")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawArc")
private external fun _nDrawArc(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    startAngle: Float,
    sweepAngle: Float,
    includeCenter: Boolean,
    paintPtr: NativePointer
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawRect")
private external fun _nDrawRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawOval")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawOval")
private external fun _nDrawOval(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawRRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawRRect")
private external fun _nDrawRRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radii: InteropPointer,
    radiiSize: Int,
    paintPtr: NativePointer
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawDRRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawDRRect")
private external fun _nDrawDRRect(
    ptr: NativePointer,
    ol: Float,
    ot: Float,
    or: Float,
    ob: Float,
    oradii: InteropPointer,
    oradiiSize: Int,
    il: Float,
    it: Float,
    ir: Float,
    ib: Float,
    iradii: InteropPointer,
    iradiiSize: Int,
    paintPtr: NativePointer
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawPath")
private external fun _nDrawPath(ptr: NativePointer, nativePath: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawImageRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawImageRect")
private external fun _nDrawImageRect(
    ptr: NativePointer,
    nativeImage: NativePointer,
    sl: Float,
    st: Float,
    sr: Float,
    sb: Float,
    dl: Float,
    dt: Float,
    dr: Float,
    db: Float,
    samplingModeVal1: Int,
    samplingModeVal2: Int,
    paintPtr: NativePointer,
    strict: Boolean
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawImageNine")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawImageNine")
private external fun _nDrawImageNine(
    ptr: NativePointer,
    nativeImage: NativePointer,
    cl: Int,
    ct: Int,
    cr: Int,
    cb: Int,
    dl: Float,
    dt: Float,
    dr: Float,
    db: Float,
    filterMode: Int,
    paintPtr: NativePointer
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawRegion")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawRegion")
private external fun _nDrawRegion(ptr: NativePointer, nativeRegion: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawString")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawString")
private external fun _nDrawString(ptr: NativePointer, string: InteropPointer, x: Float, y: Float, font: NativePointer, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawTextBlob")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawTextBlob")
private external fun _nDrawTextBlob(ptr: NativePointer, blob: NativePointer, x: Float, y: Float, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPicture")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawPicture")
private external fun _nDrawPicture(ptr: NativePointer, picturePtr: NativePointer, matrix: InteropPointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawVertices")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawVertices")
private external fun _nDrawVertices(
    ptr: NativePointer,
    verticesMode: Int,
    vertexCount: Int,
    cubics: InteropPointer,
    colors: InteropPointer,
    texCoords: InteropPointer,
    indexCount: Int,
    indices: InteropPointer,
    blendMode: Int,
    paintPtr: NativePointer
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPatch")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawPatch")
private external fun _nDrawPatch(
    ptr: NativePointer,
    cubics: InteropPointer,
    colors: InteropPointer,
    texCoords: InteropPointer,
    blendMode: Int,
    paintPtr: NativePointer
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawDrawable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawDrawable")
private external fun _nDrawDrawable(ptr: NativePointer, drawablePrt: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClear")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nClear")
private external fun _nClear(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPaint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nDrawPaint")
private external fun _nDrawPaint(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSetMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nSetMatrix")
private external fun _nSetMatrix(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetLocalToDevice")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nGetLocalToDevice")
private external fun _nGetLocalToDevice(ptr: NativePointer, resultFloats: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nResetMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nResetMatrix")
private external fun _nResetMatrix(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nClipRect")
private external fun _nClipRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    mode: Int,
    antiAlias: Boolean
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipRRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nClipRRect")
private external fun _nClipRRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radii: InteropPointer,
    size: Int,
    mode: Int,
    antiAlias: Boolean
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipPath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nClipPath")
private external fun _nClipPath(ptr: NativePointer, nativePath: NativePointer, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipRegion")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nClipRegion")
private external fun _nClipRegion(ptr: NativePointer, nativeRegion: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nTranslate")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nTranslate")
private external fun _nTranslate(ptr: NativePointer, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nScale")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nScale")
private external fun _nScale(ptr: NativePointer, sx: Float, sy: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nRotate")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nRotate")
private external fun _nRotate(ptr: NativePointer, deg: Float, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSkew")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nSkew")
private external fun _nSkew(ptr: NativePointer, sx: Float, sy: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nConcat")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nConcat")
private external fun _nConcat(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nConcat44")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nConcat44")
private external fun _nConcat44(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nReadPixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nReadPixels")
private external fun _nReadPixels(ptr: NativePointer, bitmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nWritePixels")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nWritePixels")
private external fun _nWritePixels(ptr: NativePointer, bitmapPtr: NativePointer, x: Int, y: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSave")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nSave")
private external fun _nSave(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nSaveLayer")
private external fun _nSaveLayer(ptr: NativePointer, paintPtr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayerRect")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nSaveLayerRect")
private external fun _nSaveLayerRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    paintPtr: NativePointer
): Int


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetSaveCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nGetSaveCount")
private external fun _nGetSaveCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nRestore")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nRestore")
private external fun _nRestore(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nRestoreToCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Canvas__1nRestoreToCount")
private external fun _nRestoreToCount(ptr: NativePointer, saveCount: Int)
