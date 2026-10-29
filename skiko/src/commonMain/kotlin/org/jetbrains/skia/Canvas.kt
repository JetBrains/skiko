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

    /**
     * Returns the recording context being used by this Canvas.
     *
     * The returned context is borrowed from the Canvas and must not be closed.
     *
     * @return the recording context, if available; null otherwise
     */
    val recordingContext: DirectContext?
        get() = nativeCall(_owner) {
            val ptr = _nGetCanvasRecordingContext(_ptr)
            if (ptr == NullPointer) null else DirectContext(ptr, managed = false)
        }

    /**
     * Returns the Surface this Canvas draws into.
     *
     * Canvases obtained from [Surface.canvas] return that surface. Canvases that draw into
     * a [Bitmap], a [Picture] recording or a document return null.
     *
     * The returned surface is borrowed from the Canvas and must not be closed. It carries the
     * canvas's [recordingContext], so [Surface.flush] and [Surface.flushAndSubmit] work on it.
     *
     * @return the Surface backing this Canvas, if any; null otherwise
     */
    val surface: Surface?
        get() = nativeCall(_owner) {
            val ptr = _nGetCanvasSurface(_ptr)
            if (ptr == NullPointer) null else Surface(ptr, recordingContext, managed = false)
        }

    fun drawPoint(x: Float, y: Float, paint: Paint): Canvas = nativeCall(paint) { paint ->
        _nDrawPoint(_ptr, x, y, paint._ptr)
        this
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
    fun drawPoints(coords: FloatArray, paint: Paint): Canvas = nativeCall(paint) { paint ->
        interopScope {
            _nDrawPoints(
                _ptr,
                0 /* SkCanvas::PointMode::kPoints_PointMode */,
                coords.size,
                toInterop(coords),
                paint._ptr
            )
        }
        this
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
    fun drawLines(coords: FloatArray, paint: Paint): Canvas = nativeCall(paint) { paint ->
        interopScope {
            _nDrawPoints(
                _ptr,
                1 /* SkCanvas::PointMode::kLines_PointMode */,
                coords.size,
                toInterop(coords),
                paint._ptr
            )
        }
        this
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
    fun drawPolygon(coords: FloatArray, paint: Paint): Canvas = nativeCall(paint) { paint ->
        interopScope {
            _nDrawPoints(
                _ptr,
                2 /* SkCanvas::PointMode::kPolygon_PointMode */,
                coords.size,
                toInterop(coords),
                paint._ptr
            )
        }
        this
    }

    fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float, paint: Paint): Canvas = nativeCall(paint) { paint ->
        _nDrawLine(_ptr, x0, y0, x1, y1, paint._ptr)
        this
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
    ): Canvas = nativeCall(paint) { paint ->
        _nDrawArc(_ptr, left, top, right, bottom, startAngle, sweepAngle, includeCenter, paint._ptr)
        this
    }

    fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint): Canvas = nativeCall(paint) { paint ->
        _nDrawRect(_ptr, left, top, right, bottom, paint._ptr)
        this
    }

    fun drawRect(r: Rect, paint: Paint): Canvas {
        return drawRect(r.left, r.top, r.right, r.bottom, paint)
    }

    fun drawOval(left: Float, top: Float, right: Float, bottom: Float, paint: Paint): Canvas = nativeCall(paint) { paint ->
        _nDrawOval(_ptr, left, top, right, bottom, paint._ptr)
        this
    }

    fun drawOval(r: Rect, paint: Paint): Canvas {
        return drawOval(r.left, r.top, r.right, r.bottom, paint)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, paint: Paint): Canvas = nativeCall(paint) { paint ->
        _nDrawOval(_ptr, x - radius, y - radius, x + radius, y + radius, paint._ptr)
        this
    }

    fun drawRRect(left: Float, top: Float, right: Float, bottom: Float, radii: FloatArray, paint: Paint): Canvas = nativeCall(paint) { paint ->
        interopScope {
            _nDrawRRect(_ptr, left, top, right, bottom, toInterop(radii), radii.size, paint._ptr)
        }
        this
    }

    fun drawRRect(r: RRect, paint: Paint): Canvas {
        return drawRRect(r.left, r.top, r.right, r.bottom, r.radii, paint)
    }

    fun drawDRRect(outer: RRect, inner: RRect, paint: Paint): Canvas = nativeCall(paint) { paint ->
        interopScope {
            _nDrawDRRect(
                _ptr,
                outer.left, outer.top, outer.right, outer.bottom,
                toInterop(outer.radii),
                outer.radii.size,
                inner.left, inner.top, inner.right, inner.bottom,
                toInterop(inner.radii),
                inner.radii.size,
                paint._ptr
            )
        }
        this
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

    fun drawPath(path: Path, paint: Paint): Canvas  = nativeCall(path, paint) { path, paint ->
        _nDrawPath(_ptr, path._ptr, paint._ptr)
        this
    }

    fun drawImage(image: Image, left: Float, top: Float): Canvas {
        return drawImageRect(
            image = image,
            srcLeft = 0f,
            srcTop = 0f,
            srcRight = image.width.toFloat(),
            srcBottom = image.height.toFloat(),
            dstLeft = left,
            dstTop = top,
            dstRight = left + image.width.toFloat(),
            dstBottom = top + image.height.toFloat(),
            samplingMode = SamplingMode.DEFAULT,
            paint = null,
            strict = true
        )
    }

    fun drawImage(image: Image, left: Float, top: Float, paint: Paint?): Canvas {
        return drawImageRect(
            image = image,
            srcLeft = 0f,
            srcTop = 0f,
            srcRight = image.width.toFloat(),
            srcBottom = image.height.toFloat(),
            dstLeft = left,
            dstTop = top,
            dstRight = left + image.width.toFloat(),
            dstBottom = top + image.height.toFloat(),
            samplingMode = SamplingMode.DEFAULT,
            paint = paint,
            strict = true
        )
    }

    fun drawImageRect(image: Image, dst: Rect): Canvas {
        return drawImageRect(
            image = image,
            srcLeft = 0f,
            srcTop = 0f,
            srcRight = image.width.toFloat(),
            srcBottom = image.height.toFloat(),
            dstLeft = dst.left,
            dstTop = dst.top,
            dstRight = dst.right,
            dstBottom = dst.bottom,
            samplingMode = SamplingMode.DEFAULT,
            paint = null,
            strict = true
        )
    }

    fun drawImageRect(image: Image, dst: Rect, paint: Paint?): Canvas {
        return drawImageRect(
            image = image,
            srcLeft = 0f,
            srcTop = 0f,
            srcRight = image.width.toFloat(),
            srcBottom = image.height.toFloat(),
            dstLeft = dst.left,
            dstTop = dst.top,
            dstRight = dst.right,
            dstBottom = dst.bottom,
            samplingMode = SamplingMode.DEFAULT,
            paint = paint,
            strict = true
        )
    }

    fun drawImageRect(image: Image, src: Rect, dst: Rect): Canvas {
        return drawImageRect(
            image = image,
            srcLeft = src.left,
            srcTop = src.top,
            srcRight = src.right,
            srcBottom = src.bottom,
            dstLeft = dst.left,
            dstTop = dst.top,
            dstRight = dst.right,
            dstBottom = dst.bottom,
            samplingMode = SamplingMode.DEFAULT,
            paint = null,
            strict = true
        )
    }

    fun drawImageRect(image: Image, src: Rect, dst: Rect, paint: Paint?): Canvas {
        return drawImageRect(
            image = image,
            srcLeft = src.left,
            srcTop = src.top,
            srcRight = src.right,
            srcBottom = src.bottom,
            dstLeft = dst.left,
            dstTop = dst.top,
            dstRight = dst.right,
            dstBottom = dst.bottom,
            samplingMode = SamplingMode.DEFAULT,
            paint = paint,
            strict = true
        )
    }

    fun drawImageRect(image: Image, src: Rect, dst: Rect, paint: Paint?, strict: Boolean): Canvas {
        return drawImageRect(
            image = image,
            srcLeft = src.left,
            srcTop = src.top,
            srcRight = src.right,
            srcBottom = src.bottom,
            dstLeft = dst.left,
            dstTop = dst.top,
            dstRight = dst.right,
            dstBottom = dst.bottom,
            samplingMode = SamplingMode.DEFAULT,
            paint = paint,
            strict = strict
        )
    }

    fun drawImageRect(
        image: Image,
        srcLeft: Float,
        srcTop: Float,
        srcRight: Float,
        srcBottom: Float,
        dstLeft: Float,
        dstTop: Float,
        dstRight: Float,
        dstBottom: Float,
        samplingMode: SamplingMode,
        paint: Paint?,
        strict: Boolean
    ): Canvas = nativeCall(image, paint) { image, paint ->
        _nDrawImageRect(
            _ptr,
            image._ptr,
            srcLeft,
            srcTop,
            srcRight,
            srcBottom,
            dstLeft,
            dstTop,
            dstRight,
            dstBottom,
            samplingMode._packedInt1(),
            samplingMode._packedInt2(),
            paint.pointerOrNullPointer,
            strict
        )
        this
    }

    fun drawImageRect(
        image: Image,
        src: Rect,
        dst: Rect,
        samplingMode: SamplingMode,
        paint: Paint?,
        strict: Boolean
    ): Canvas {
        return drawImageRect(
            image = image,
            srcLeft = src.left,
            srcTop = src.top,
            srcRight = src.right,
            srcBottom = src.bottom,
            dstLeft = dst.left,
            dstTop = dst.top,
            dstRight = dst.right,
            dstBottom = dst.bottom,
            samplingMode = samplingMode,
            paint = paint,
            strict = strict
        )
    }

    fun drawImageNine(image: Image, center: IRect, dst: Rect, filterMode: FilterMode, paint: Paint?): Canvas =
        nativeCall(image, paint) { image, paint ->
            _nDrawImageNine(
                _ptr,
                image._ptr,
                center.left,
                center.top,
                center.right,
                center.bottom,
                dst.left,
                dst.top,
                dst.right,
                dst.bottom,
                filterMode.ordinal,
                paint.pointerOrNullPointer
            )
            this
        }

    fun drawRegion(r: Region, paint: Paint): Canvas = nativeCall(r, paint) { r, paint ->
        _nDrawRegion(_ptr, r._ptr, paint._ptr)
        this
    }

    fun drawString(s: String, x: Float, y: Float, font: Font?, paint: Paint): Canvas = nativeCall(font, paint) { font, paint ->
        interopScope {
            _nDrawString(_ptr, toInterop(s), x, y, font.pointerOrNullPointer, paint._ptr)
        }
        this
    }

    /**
     * Draw a text [blob] with baseline starting at [x] [y] with [paint]
     */
    fun drawTextBlob(blob: TextBlob, x: Float, y: Float, paint: Paint): Canvas = nativeCall(blob, paint) { blob, paint ->
        _nDrawTextBlob(_ptr, blob._ptr, x, y, paint._ptr)
        this
    }

    /**
     * Draw a text [line] with baseline starting at [x] [y] with [paint]
     */
    fun drawTextLine(line: TextLine, x: Float, y: Float, paint: Paint): Canvas {
        line.textBlob?.use { blob -> drawTextBlob(blob, x, y, paint) }
        return this
    }

    fun drawPicture(picture: Picture, matrix: Matrix33? = null, paint: Paint? = null): Canvas = nativeCall(picture, paint) { picture, paint ->
        interopScope {
            _nDrawPicture(_ptr, picture._ptr, toInterop(matrix?.mat), paint.pointerOrNullPointer)
        }
        this
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
       return nativeCall(paint) { paint ->
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
                    paint._ptr
                )
            }
            this
        }
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
        return nativeCall(paint) { paint ->
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
                    paint._ptr
                )
            }
            this
        }
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
        return nativeCall(paint) { paint ->
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
                    paint._ptr
                )
            }
            this
        }
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
        return nativeCall(paint) { paint ->
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
                    paint._ptr
                )
            }
            this
        }
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
        return nativeCall(paint) { paint ->
            interopScope {
                _nDrawPatch(
                    _ptr,
                    toInterop(Point.flattenArray(cubics)),
                    toInterop(colors),
                    toInterop(Point.flattenArray(texCoords)),
                    blendMode.ordinal,
                    paint._ptr
                )
            }
            this
        }
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
    fun drawDrawable(drawable: Drawable, matrix: Matrix33?): Canvas = nativeCall(drawable) { drawable ->
        interopScope {
            _nDrawDrawable(_ptr, drawable._ptr, toInterop(matrix?.mat))
        }
        this
    }

    /**
     * Associates [key] and [value] with [rect] in the drawing stream.
     *
     * The annotation draws nothing itself. It is preserved when the canvas records into a [Picture]
     * and is delivered again when that picture is played back, which makes it usable as a marker in
     * a recorded drawing. A canvas that does not recognize the key ignores the annotation.
     *
     * @param rect   the region the annotation applies to
     * @param key    identifies the annotation's meaning to whoever consumes the drawing
     * @param value  the annotation's data; may be null
     */
    fun drawAnnotation(rect: Rect, key: String, value: Data? = null): Canvas = nativeCall(value) { value ->
        interopScope {
            _nDrawAnnotation(_ptr, rect.left, rect.top, rect.right, rect.bottom, toInterop(key), value.pointerOrNullPointer)
        }
        this
    }

    fun clear(color: Int): Canvas = nativeCall {
        _nClear(_ptr, color)
        this
    }

    fun drawPaint(paint: Paint): Canvas = nativeCall(paint) { paint ->
        _nDrawPaint(_ptr, paint._ptr)
        this
    }

    /**
     * Replaces Matrix with matrix.
     * Unlike concat(), any prior matrix state is overwritten.
     *
     * @param matrix  matrix to copy, replacing existing Matrix
     *
     * @see [https://fiddle.skia.org/c/@Canvas_setMatrix](https://fiddle.skia.org/c/@Canvas_setMatrix)
     */
    fun setMatrix(matrix: Matrix33): Canvas = nativeCall {
        interopScope {
            _nSetMatrix(_ptr, toInterop(matrix.mat))
        }
        this
    }

    /**
     * Sets SkMatrix to the identity matrix.
     * Any prior matrix state is overwritten.
     *
     * @see [https://fiddle.skia.org/c/@Canvas_resetMatrix](https://fiddle.skia.org/c/@Canvas_resetMatrix)
     */
    fun resetMatrix(): Canvas = nativeCall {
        _nResetMatrix(_ptr)
        this
    }

    /**
     * Returns the total transformation matrix for the canvas.
     */
    val localToDevice: Matrix44
        get() = nativeCall {
            Matrix44.fromInteropPointer { interopPointer -> _nGetLocalToDevice(_ptr, interopPointer) }
        }

    val localToDeviceAsMatrix33: Matrix33
        get() = localToDevice.asMatrix33()

    fun clipRect(r: Rect, mode: ClipMode, antiAlias: Boolean): Canvas {
      return clipRect(r.left, r.top, r.right, r.bottom, mode, antiAlias)
    }

    fun clipRect(left: Float, top: Float, right: Float, bottom: Float, mode: ClipMode, antiAlias: Boolean): Canvas = nativeCall {
        _nClipRect(_ptr, left, top, right, bottom, mode.ordinal, antiAlias)
        this
    }

    fun clipRect(r: Rect, mode: ClipMode): Canvas {
        return clipRect(r.left, r.top, r.right, r.bottom, mode, false)
    }

    fun clipRect(r: Rect, antiAlias: Boolean): Canvas {
        return clipRect(r.left, r.top, r.right, r.bottom, ClipMode.INTERSECT, antiAlias)
    }

    fun clipRect(left: Float, top: Float, right: Float, bottom: Float, antiAlias: Boolean): Canvas {
        return clipRect(left, top, right, bottom, ClipMode.INTERSECT, antiAlias)
    }

    fun clipRect(r: Rect): Canvas {
        return clipRect(r.left, r.top, r.right, r.bottom, ClipMode.INTERSECT, false)
    }

    fun clipRRect(r: RRect, mode: ClipMode, antiAlias: Boolean): Canvas {
        return clipRRect(r.left, r.top, r.right, r.bottom, r.radii, mode, antiAlias)
    }

    fun clipRRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radii: FloatArray,
        mode: ClipMode,
        antiAlias: Boolean
    ): Canvas = nativeCall {
        interopScope {
            _nClipRRect(_ptr, left, top, right, bottom, toInterop(radii), radii.size, mode.ordinal, antiAlias)
        }
        this
    }

    fun clipRRect(r: RRect, mode: ClipMode): Canvas {
        return clipRRect(r.left, r.top, r.right, r.bottom, r.radii, mode, false)
    }

    fun clipRRect(left: Float, top: Float, right: Float, bottom: Float, radii: FloatArray, mode: ClipMode): Canvas {
        return clipRRect(left, top, right, bottom, radii, mode, false)
    }

    fun clipRRect(r: RRect, antiAlias: Boolean): Canvas {
        return clipRRect(r.left, r.top, r.right, r.bottom, r.radii, ClipMode.INTERSECT, antiAlias)
    }

    fun clipRRect(left: Float, top: Float, right: Float, bottom: Float, radii: FloatArray, antiAlias: Boolean): Canvas {
        return clipRRect(left, top, right, bottom, radii, ClipMode.INTERSECT, antiAlias)
    }

    fun clipRRect(left: Float, top: Float, right: Float, bottom: Float, radii: FloatArray): Canvas {
        return clipRRect(left, top, right, bottom, radii, ClipMode.INTERSECT, false)
    }

    fun clipPath(p: Path, mode: ClipMode, antiAlias: Boolean): Canvas = nativeCall(p) { p ->
        _nClipPath(_ptr, p._ptr, mode.ordinal, antiAlias)
        this
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

    fun clipRegion(r: Region, mode: ClipMode): Canvas = nativeCall(r) { r ->
        _nClipRegion(_ptr, r._ptr, mode.ordinal)
        this
    }

    fun clipRegion(r: Region): Canvas {
        return clipRegion(r, ClipMode.INTERSECT)
    }

    /**
     * Returns bounds of clip in local coordinates.
     *
     * The returned [Rect] is outset by one device pixel to account for partial pixel coverage
     * in case the clip is anti-aliased.
     *
     * If the clip is empty, returns an empty [Rect].
     */
    val localClipBounds: Rect
        get() = nativeCall { Rect.fromInteropPointer { _nGetLocalClipBounds(_ptr, it) } }

    fun translate(dx: Float, dy: Float): Canvas = nativeCall {
        _nTranslate(_ptr, dx, dy)
        this
    }

    fun scale(sx: Float, sy: Float): Canvas = nativeCall {
        _nScale(_ptr, sx, sy)
        this
    }

    /**
     * @param deg  angle in degrees
     * @return     this
     */
    fun rotate(deg: Float): Canvas = nativeCall {
        _nRotate(_ptr, deg, 0f, 0f)
        this
    }

    fun rotate(deg: Float, x: Float, y: Float): Canvas = nativeCall {
        _nRotate(_ptr, deg, x, y)
        this
    }

    fun skew(sx: Float, sy: Float): Canvas = nativeCall {
        _nSkew(_ptr, sx, sy)
        this
    }

    fun concat(matrix: Matrix33): Canvas = nativeCall {
        interopScope {
            _nConcat(_ptr, toInterop(matrix.mat))
        }
        this
    }

    fun concat(matrix: Matrix44): Canvas = nativeCall {
        interopScope {
            _nConcat44(_ptr, toInterop(matrix.mat))
        }
        this
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
    fun readPixels(bitmap: Bitmap, srcX: Int, srcY: Int): Boolean = nativeCall(bitmap) { bitmap ->
        _nReadPixels(_ptr, bitmap._ptr, srcX, srcY)
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
    fun writePixels(bitmap: Bitmap, x: Int, y: Int): Boolean = nativeCall(bitmap) { bitmap ->
        _nWritePixels(_ptr, bitmap._ptr, x, y)
    }

    fun save(): Int = nativeCall { _nSave(_ptr) }

    fun saveLayer(left: Float, top: Float, right: Float, bottom: Float, paint: Paint?): Int = nativeCall(paint) { paint ->
        _nSaveLayerRect(_ptr, left, top, right, bottom, paint.pointerOrNullPointer)
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
    fun saveLayer(bounds: Rect?, paint: Paint?): Int = nativeCall(paint) { paint ->
        if (bounds == null) _nSaveLayer(_ptr, paint.pointerOrNullPointer)
        else _nSaveLayerRect(_ptr, bounds.left, bounds.top, bounds.right, bounds.bottom, paint.pointerOrNullPointer)
    }

    fun saveLayer(layerRec: SaveLayerRec): Int = nativeCall(layerRec) { layerRec ->
        if (layerRec.bounds != null) {
            _nSaveLayerSaveLayerRecRect(
                _ptr,
                layerRec.bounds.left,
                layerRec.bounds.top,
                layerRec.bounds.right,
                layerRec.bounds.bottom,
                layerRec.paint.pointerOrNullPointer,
                layerRec.backdrop.pointerOrNullPointer,
                layerRec.colorSpace.pointerOrNullPointer,
                layerRec.saveLayerFlags.mask
            )
        } else {
            _nSaveLayerSaveLayerRec(
                _ptr,
                layerRec.paint.pointerOrNullPointer,
                layerRec.backdrop.pointerOrNullPointer,
                layerRec.colorSpace.pointerOrNullPointer,
                layerRec.saveLayerFlags.mask
            )
        }
    }

    val saveCount: Int
        get() = nativeCall { _nGetSaveCount(_ptr) }

    fun restore(): Canvas = nativeCall {
        _nRestore(_ptr)
        this
    }

    fun restoreToCount(saveCount: Int): Canvas = nativeCall {
        _nRestoreToCount(_ptr, saveCount)
        this
    }

    class SaveLayerRec(
        val bounds: Rect? = null,
        val paint: Paint? = null,
        val backdrop: ImageFilter? = null,
        val colorSpace: ColorSpace? = null,
        val saveLayerFlags: SaveLayerFlags = SaveLayerFlags()
    )

    enum class SaveLayerFlagsSet(val mask: Int) {
        PreserveLCDText(1 shl 1),
        InitWithPrevious(1 shl 2),
        F16ColorType(1 shl 4)
    }

    class SaveLayerFlags internal constructor(internal val mask: Int) {
        constructor(vararg flagsSet: SaveLayerFlagsSet) : this(flagsSet.fold(0) { acc, flag -> acc or flag.mask })

        operator fun contains(flag: SaveLayerFlagsSet): Boolean = (mask and flag.mask) != 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as SaveLayerFlags

            return mask == other.mask
        }

        override fun hashCode(): Int {
            return mask
        }
    }

    private object _FinalizerHolder {
        val PTR = Canvas_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetFinalizer")
// No ForceNativeThreadState: returns the function that frees the object, without calling it, so it is short and cannot
// block.
// Native call path: deleteCanvas
private external fun Canvas_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nMakeFromBitmap")
// Needs ForceNativeThreadState: first use initializes a static object, and concurrent first calls can wait for another
// thread to finish.
// Native call path: SkBitmapDevice::SkBitmapDevice -> skcpu::Recorder::TODO
@ForceNativeThreadState
private external fun _nMakeFromBitmap(bitmapPtr: NativePointer, flags: Int, pixelGeometry: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetRecordingContext")
// Needs ForceNativeThreadState: follows a chain of nested canvas wrappers, so its time grows with how deeply they are
// nested.
// Native call path: SkPaintFilterCanvas::recordingContext -> SkPaintFilterCanvas::recordingContext
@ForceNativeThreadState
private external fun _nGetCanvasRecordingContext(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetSurface")
// No ForceNativeThreadState: returns the stored surface pointer, so it is short and cannot block.
// Native call path: SkCanvas::getSurface
private external fun _nGetCanvasSurface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPoint")
// Needs ForceNativeThreadState: draws points on a canvas that can be GPU-backed, which can call Metal.
// Native call path: SkCanvas::onDrawPoints -> SkDevice::drawPoints
@ForceNativeThreadState
private external fun _nDrawPoint(ptr: NativePointer, x: Float, y: Float, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPoints")
// Needs ForceNativeThreadState: draws points on a canvas that can be GPU-backed, which can call Metal.
// Native call path: SkCanvas::onDrawPoints -> SkDevice::drawPoints
@ForceNativeThreadState
private external fun _nDrawPoints(ptr: NativePointer, mode: Int, coordsCount: Int, coords: InteropPointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawLine")
// Needs ForceNativeThreadState: draws a line on a canvas that can be GPU-backed, which can call Metal.
// Native call path: SkCanvas::drawLine -> SkCanvas::onDrawPoints -> SkDevice::drawPoints
@ForceNativeThreadState
private external fun _nDrawLine(ptr: NativePointer, x0: Float, y0: Float, x1: Float, y1: Float, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawArc")
// Needs ForceNativeThreadState: draws an arc on a canvas that can be GPU-backed, which can call Metal.
// Native call path: SkCanvas::onDrawArc -> SkDevice::drawArc
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: draws a rectangle on a canvas that can be GPU-backed, which can call Metal.
// Native call path: SkCanvas::onDrawRect -> SkDevice::drawRect
@ForceNativeThreadState
private external fun _nDrawRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawOval")
// Needs ForceNativeThreadState: draws an oval on a canvas that can be GPU-backed, which can call Metal.
// Native call path: SkCanvas::onDrawOval -> SkDevice::drawOval
@ForceNativeThreadState
private external fun _nDrawOval(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawRRect")
// Needs ForceNativeThreadState: draws a rounded rectangle on a canvas that can be GPU-backed, which can call Metal.
// Native call path: SkCanvas::onDrawRRect -> SkDevice::drawRRect
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: draws a double rounded rectangle on a canvas that can be GPU-backed, which can call
// Metal.
// Native call path: SkCanvas::onDrawDRRect -> SkDevice::drawDRRect
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: draws a path on a canvas that can be GPU-backed, which can call Metal.
// Native call path: SkCanvas::onDrawPath -> SkDevice::drawPath
@ForceNativeThreadState
private external fun _nDrawPath(ptr: NativePointer, nativePath: NativePointer, paintPtr: NativePointer)

// Needs ForceNativeThreadState: drawing the bitmap can upload it to a GPU texture through Metal.
// Native call path: SkImages::RasterFromBitmap -> SkCanvas::drawImageRect

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawImageRect")
// Needs ForceNativeThreadState: drawing the image can upload it to a GPU texture through Metal.
// Native call path: SkCanvas::onDrawImageRect2 -> GrProxyProvider -> GrMtlGpu::onCreateTexture
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: drawing the image in nine pieces can upload it to a GPU texture through Metal.
// Native call path: SkCanvas::drawImageNine -> SkCanvas::onDrawImageLattice2 -> GrMtlGpu::uploadToTexture
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: draws a region on a canvas that can be GPU-backed, which can call Metal.
// Native call path: SkCanvas::onDrawRegion -> SkDevice::drawRegion -> SkDevice::drawPath
@ForceNativeThreadState
private external fun _nDrawRegion(ptr: NativePointer, nativeRegion: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawString")
// Needs ForceNativeThreadState: maps characters to glyphs through CoreText, then draws the text on a canvas that can be
// GPU-backed.
// Native call path: SkCanvas::drawSimpleText -> GlyphRunBuilder::textToGlyphRunList -> SkTypeface_Mac::onCharsToGlyphs
@ForceNativeThreadState
private external fun _nDrawString(ptr: NativePointer, string: InteropPointer, x: Float, y: Float, font: NativePointer, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawTextBlob")
// Needs ForceNativeThreadState: holds a glyph cache lock while CoreText renders missing glyphs, and can also draw on a
// GPU-backed canvas.
// Native call path: SkCanvas::drawTextBlob -> SkCanvas::onDrawGlyphRunList -> SkScalerContext_Mac::generateImage
@ForceNativeThreadState
private external fun _nDrawTextBlob(ptr: NativePointer, blob: NativePointer, x: Float, y: Float, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPicture")
// Needs ForceNativeThreadState: replays recorded drawing operations on a canvas that can use the GPU, so its time grows
// with the picture size.
// Native call path: SkCanvas::onDrawPicture -> SkPicture::playback
@ForceNativeThreadState
private external fun _nDrawPicture(ptr: NativePointer, picturePtr: NativePointer, matrix: InteropPointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawVertices")
// Needs ForceNativeThreadState: copies the vertex arrays, so its time grows with their size, and draws on a canvas that
// can be GPU-backed.
// Native call path: SkVertices::MakeCopy -> SkCanvas::onDrawVerticesObject -> SkDevice::drawVertices
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: draws on a canvas that can be GPU-backed, which can call Metal, and may first copy a
// cached image.
// Native call path: SkCanvas::onDrawPatch -> SkPatchUtils::MakeVertices -> drawVertices
@ForceNativeThreadState
private external fun _nDrawPatch(
    ptr: NativePointer,
    cubics: InteropPointer,
    colors: InteropPointer,
    texCoords: InteropPointer,
    blendMode: Int,
    paintPtr: NativePointer
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawDrawable")
// Needs ForceNativeThreadState: calls back into Kotlin to draw the drawable.
// Native call path: SkCanvas::onDrawDrawable -> SkDrawable::draw -> SkikoDrawable::onDraw
@ForceNativeThreadState
private external fun _nDrawDrawable(ptr: NativePointer, drawablePrt: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawAnnotation")
// Needs ForceNativeThreadState: can drop the last reference to a cached GPU image, or copy it on write, before drawing.
// Native call path: SkCanvas::onDrawAnnotation -> predrawNotify -> SkSurface_Base::aboutToDraw
@ForceNativeThreadState
private external fun _nDrawAnnotation(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    key: InteropPointer,
    valuePtr: NativePointer
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClear")
// Needs ForceNativeThreadState: fills a canvas that can use the GPU and may first copy or release a cached image.
// Native call path: SkCanvas::clear -> drawColor -> onDrawPaint -> SkDevice::drawPaint
@ForceNativeThreadState
private external fun _nClear(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPaint")
// Needs ForceNativeThreadState: fills the canvas, which can be GPU-backed and call Metal, and may first copy or drop a
// cached image.
// Native call path: SkCanvas::onDrawPaint -> SkDevice::drawPaint
@ForceNativeThreadState
private external fun _nDrawPaint(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSetMatrix")
// Needs ForceNativeThreadState: can reallocate and copy the recorded command array, which can be arbitrarily large, so
// its time grows with the number of recorded commands.
// Native call path: SkRecordCanvas::didSetM44 -> SkRecord::append -> SkRecord::grow
@ForceNativeThreadState
private external fun _nSetMatrix(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetLocalToDevice")
// No ForceNativeThreadState: returns a stored matrix and copies 16 floats out, so it is short and cannot block.
// Native call path: SkCanvas::getLocalToDevice
private external fun _nGetLocalToDevice(ptr: NativePointer, resultFloats: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nResetMatrix")
// Needs ForceNativeThreadState: can reallocate and copy the recorded command array, which can be arbitrarily large, so
// its time grows with the number of recorded commands.
// Native call path: SkRecordCanvas::didSetM44 -> SkRecord::append -> SkRecord::grow
@ForceNativeThreadState
private external fun _nResetMatrix(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipRect")
// Needs ForceNativeThreadState: on a raster canvas, combines the clip with the rectangle through region operations, so
// its time grows with the clip complexity.
// Native call path: SkCanvas::onClipRect -> SkBitmapDevice::clipRect -> SkRasterClip::op -> SkRegion::op
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: on a raster canvas, turns the rounded rectangle into a pixel coverage mask, so its time
// grows with the clip area.
// Native call path: SkCanvas::onClipRRect -> SkBitmapDevice::clipRRect -> SkRasterClip::op -> SkAAClip::setPath
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: on a raster canvas, turns the path into a pixel coverage mask, so its time grows with
// the path and clip size.
// Native call path: SkCanvas::onClipPath -> SkBitmapDevice::clipPath -> SkRasterClip::op -> SkAAClip::setPath
@ForceNativeThreadState
private external fun _nClipPath(ptr: NativePointer, nativePath: NativePointer, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipRegion")
// Needs ForceNativeThreadState: on a raster canvas, combines the clip with the region, so its time grows with the
// region complexity.
// Native call path: SkCanvas::onClipRegion -> SkBitmapDevice::clipRegion -> SkRasterClip::op -> SkRegion::op
@ForceNativeThreadState
private external fun _nClipRegion(ptr: NativePointer, nativeRegion: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetLocalClipBounds")
// No ForceNativeThreadState: reads the stored clip bounds and inverts one matrix, so it is short and cannot block.
// Native call path: SkCanvas::getLocalClipBounds -> SkDevice::devClipBounds
private external fun _nGetLocalClipBounds(ptr: NativePointer, rectPtr: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nTranslate")
// Needs ForceNativeThreadState: can reallocate and copy the recorded command array, which can be arbitrarily large, so
// its time grows with the number of recorded commands.
// Native call path: SkRecordCanvas::didTranslate -> SkRecord::append -> SkRecord::grow
@ForceNativeThreadState
private external fun _nTranslate(ptr: NativePointer, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nScale")
// Needs ForceNativeThreadState: can reallocate and copy the recorded command array, which can be arbitrarily large, so
// its time grows with the number of recorded commands.
// Native call path: SkRecordCanvas::didScale -> SkRecord::append -> SkRecord::grow
@ForceNativeThreadState
private external fun _nScale(ptr: NativePointer, sx: Float, sy: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nRotate")
// Needs ForceNativeThreadState: can reallocate and copy the recorded command array, which can be arbitrarily large, so
// its time grows with the number of recorded commands.
// Native call path: SkRecordCanvas::didConcat44 -> SkRecord::append -> SkRecord::grow
@ForceNativeThreadState
private external fun _nRotate(ptr: NativePointer, deg: Float, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSkew")
// Needs ForceNativeThreadState: can reallocate and copy the recorded command array, which can be arbitrarily large, so
// its time grows with the number of recorded commands.
// Native call path: SkRecordCanvas::didConcat44 -> SkRecord::append -> SkRecord::grow
@ForceNativeThreadState
private external fun _nSkew(ptr: NativePointer, sx: Float, sy: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nConcat")
// Needs ForceNativeThreadState: can reallocate and copy the recorded command array, which can be arbitrarily large, so
// its time grows with the number of recorded commands.
// Native call path: SkRecordCanvas::didConcat44 -> SkRecord::append -> SkRecord::grow
@ForceNativeThreadState
private external fun _nConcat(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nConcat44")
// Needs ForceNativeThreadState: can reallocate and copy the recorded command array, which can be arbitrarily large, so
// its time grows with the number of recorded commands.
// Native call path: SkRecordCanvas::didConcat44 -> SkRecord::append -> SkRecord::grow
@ForceNativeThreadState
private external fun _nConcat44(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nReadPixels")
// Needs ForceNativeThreadState: reads pixels back from a GPU surface through Metal, which can wait on the GPU queue, or
// converts every pixel on a raster canvas.
// Native call path: SkCanvas::readPixels -> SkDevice::readPixels -> GrMtlGpu::onReadPixels
@ForceNativeThreadState
private external fun _nReadPixels(ptr: NativePointer, bitmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nWritePixels")
// Needs ForceNativeThreadState: uploads pixels to a GPU texture through Metal, or converts every pixel on a raster
// canvas.
// Native call path: SkCanvas::writePixels -> predrawNotify -> SkDevice::writePixels -> GrMtlGpu::uploadToTexture
@ForceNativeThreadState
private external fun _nWritePixels(ptr: NativePointer, bitmapPtr: NativePointer, x: Int, y: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSave")
// Needs ForceNativeThreadState: in debug builds, walks every saved state on the stack, so its time grows with the save
// depth.
// Native call path: SkCanvas::save -> SkCanvas::getSaveCount
@ForceNativeThreadState
private external fun _nSave(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayer")
// Needs ForceNativeThreadState: allocates a drawing layer on the GPU or in memory and can call a Kotlin paint filter.
// Native call path: SkCanvas::saveLayer -> internalSaveLayer -> SkDevice::createDevice
@ForceNativeThreadState
private external fun _nSaveLayer(ptr: NativePointer, paintPtr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayerRect")
// Needs ForceNativeThreadState: allocates a drawing layer on the GPU or in memory and can call a Kotlin paint filter.
// Native call path: SkCanvas::saveLayer -> internalSaveLayer -> SkDevice::createDevice
@ForceNativeThreadState
private external fun _nSaveLayerRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    paintPtr: NativePointer
): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayerSaveLayerRec")
// Needs ForceNativeThreadState: allocates a drawing layer on the GPU or in memory and can filter the image behind the
// layer.
// Native call path: SkCanvas::saveLayer -> internalSaveLayer -> SkDevice::createDevice
@ForceNativeThreadState
private external fun _nSaveLayerSaveLayerRec(
    ptr: NativePointer,
    paintPtr: NativePointer,
    backdropImageFilterPtr: NativePointer,
    colorSpacePtr: NativePointer,
    saveLayerFlags: Int
): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayerSaveLayerRecRect")
// Needs ForceNativeThreadState: allocates a drawing layer on the GPU or in memory and can filter the image behind the
// layer.
// Native call path: SkCanvas::saveLayer -> internalSaveLayer -> SkDevice::createDevice
@ForceNativeThreadState
private external fun _nSaveLayerSaveLayerRecRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    paintPtr: NativePointer,
    backdropImageFilterPtr: NativePointer,
    colorSpacePtr: NativePointer,
    saveLayerFlags: Int
): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetSaveCount")
// Needs ForceNativeThreadState: in debug builds, walks every saved state on the stack, so its time grows with the save
// depth.
// Native call path: SkCanvas::getSaveCount
@ForceNativeThreadState
private external fun _nGetSaveCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nRestore")
// Needs ForceNativeThreadState: composites the pending layer on a GPU or raster device, then destroys the layer, which
// frees GPU objects.
// Native call path: SkCanvas::restore -> internalRestore -> drawDevice
@ForceNativeThreadState
private external fun _nRestore(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nRestoreToCount")
// Needs ForceNativeThreadState: restores repeatedly, compositing each pending layer on a GPU or raster device and
// destroying it, which frees GPU objects.
// Native call path: SkCanvas::restoreToCount -> SkCanvas::restore -> internalRestore
@ForceNativeThreadState
private external fun _nRestoreToCount(ptr: NativePointer, saveCount: Int)

internal inline fun Canvas.runRestoringState(block: Canvas.() -> Unit) {
    val restoreCount = save()
    try {
        block()
    } finally {
        restoreToCount(restoreCount)
    }
}
