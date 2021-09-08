@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NULLPNTR
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import kotlin.jvm.JvmStatic
import kotlin.math.round

class Paint : Managed {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetFinalizer")
        external fun _nGetFinalizer(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nMake")
        external fun _nMake(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nMakeClone")
        external fun _nMakeClone(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nEquals")
        external fun _nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nReset")
        external fun _nReset(ptr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nIsAntiAlias")
        external fun _nIsAntiAlias(ptr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetAntiAlias")
        external fun _nSetAntiAlias(ptr: NativePointer, value: Boolean)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nIsDither")
        external fun _nIsDither(ptr: NativePointer): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetDither")
        external fun _nSetDither(ptr: NativePointer, value: Boolean)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetMode")
        external fun _nGetMode(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetMode")
        external fun _nSetMode(ptr: NativePointer, value: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetColor")
        external fun _nGetColor(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetColor4f")
        external fun _nGetColor4f(ptr: NativePointer): Color4f
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetColor")
        external fun _nSetColor(ptr: NativePointer, argb: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetColor4f")
        external fun _nSetColor4f(ptr: NativePointer, r: Float, g: Float, b: Float, a: Float, colorSpacePtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeWidth")
        external fun _nGetStrokeWidth(ptr: NativePointer): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeWidth")
        external fun _nSetStrokeWidth(ptr: NativePointer, value: Float)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeMiter")
        external fun _nGetStrokeMiter(ptr: NativePointer): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeMiter")
        external fun _nSetStrokeMiter(ptr: NativePointer, value: Float)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeCap")
        external fun _nGetStrokeCap(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeCap")
        external fun _nSetStrokeCap(ptr: NativePointer, value: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeJoin")
        external fun _nGetStrokeJoin(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeJoin")
        external fun _nSetStrokeJoin(ptr: NativePointer, value: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetFillPath")
        external fun _nGetFillPath(ptr: NativePointer, path: NativePointer, resScale: Float): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetFillPathCull")
        external fun _nGetFillPathCull(
            ptr: NativePointer,
            path: NativePointer,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            resScale: Float
        ): NativePointer

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetShader")
        external fun _nGetShader(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetShader")
        external fun _nSetShader(ptr: NativePointer, shaderPtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetColorFilter")
        external fun _nGetColorFilter(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetColorFilter")
        external fun _nSetColorFilter(ptr: NativePointer, colorFilterPtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetBlendMode")
        external fun _nGetBlendMode(ptr: NativePointer): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetBlendMode")
        external fun _nSetBlendMode(ptr: NativePointer, mode: Int)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetPathEffect")
        external fun _nGetPathEffect(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetPathEffect")
        external fun _nSetPathEffect(ptr: NativePointer, pathEffectPtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetMaskFilter")
        external fun _nGetMaskFilter(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetMaskFilter")
        external fun _nSetMaskFilter(ptr: NativePointer, filterPtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nGetImageFilter")
        external fun _nGetImageFilter(ptr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nSetImageFilter")
        external fun _nSetImageFilter(ptr: NativePointer, filterPtr: NativePointer)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paint__1nHasNothingToDraw")
        external fun _nHasNothingToDraw(ptr: NativePointer): Boolean

        init {
            staticLoad()
        }
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    internal constructor(ptr: NativePointer, managed: Boolean) : super(ptr, _FinalizerHolder.PTR, managed)

    /**
     * Constructs SkPaint with default values.
     *
     * @see [https://fiddle.skia.org/c/@Paint_empty_constructor](https://fiddle.skia.org/c/@Paint_empty_constructor)
     */
    constructor() : super(_nMake(), _FinalizerHolder.PTR) {
        Stats.onNativeCall()
    }

    /**
     *
     * Makes a shallow copy of Paint. PathEffect, Shader,
     * MaskFilter, ColorFilter, and ImageFilter are shared
     * between the original paint and the copy.
     *
     *
     * The referenced objects PathEffect, Shader, MaskFilter, ColorFilter,
     * and ImageFilter cannot be modified after they are created.
     *
     * @return  shallow copy of paint
     *
     * @see [https://fiddle.skia.org/c/@Paint_copy_const_SkPaint](https://fiddle.skia.org/c/@Paint_copy_const_SkPaint)
     */
    fun makeClone(): Paint {
        return try {
            Stats.onNativeCall()
            Paint(_nMakeClone(_ptr), true)
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            _nEquals(_ptr, getPtr(other))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(other)
        }
    }

    /**
     * Sets all Paint contents to their initial values. This is equivalent to replacing
     * Paint with the result of Paint().
     *
     * @see [https://fiddle.skia.org/c/@Paint_reset](https://fiddle.skia.org/c/@Paint_reset)
     */
    fun reset(): Paint {
        Stats.onNativeCall()
        _nReset(_ptr)
        return this
    }

    /**
     * Requests, but does not require, that edge pixels draw opaque or with partial transparency.
     *
     * Returns true if pixels on the active edges of Path may be drawn with partial transparency.
     *
     * @return  antialiasing state
     */
    var isAntiAlias: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsAntiAlias(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setAntiAlias(value)
        }
    
    /**
     * Requests, but does not require, that edge pixels draw opaque or with partial transparency.
     *
     * @param value  setting for antialiasing
     */
    fun setAntiAlias(value: Boolean): Paint {
        Stats.onNativeCall()
        _nSetAntiAlias(_ptr, value)
        return this
    }

    /**
     * Requests, but does not require, to distribute color error.
     *
     * @return  true if color error may be distributed to smooth color transition.
     */
    var isDither: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsDither(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setDither(value)
        }

    /**
     * Requests, but does not require, to distribute color error.
     *
     * @param value  setting for ditering
     * @return       this
     */
    fun setDither(value: Boolean): Paint {
        Stats.onNativeCall()
        _nSetDither(_ptr, value)
        return this
    }

    /**
     * Sets whether the geometry is filled, stroked, or filled and stroked.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStyle](https://fiddle.skia.org/c/@Paint_setStyle)
     *
     * @see [https://fiddle.skia.org/c/@Stroke_Width](https://fiddle.skia.org/c/@Stroke_Width)
     *
     * @return  whether the geometry is filled, stroked, or filled and stroked.
     */
    var mode: PaintMode
        get() = try {
            Stats.onNativeCall()
            PaintMode.values().get(_nGetMode(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setMode(value)
        }

    /**
     * Sets whether the geometry is filled, stroked, or filled and stroked.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStyle](https://fiddle.skia.org/c/@Paint_setStyle)
     *
     * @see [https://fiddle.skia.org/c/@Stroke_Width](https://fiddle.skia.org/c/@Stroke_Width)
     */
    fun setMode(style: PaintMode): Paint {
        Stats.onNativeCall()
        _nSetMode(_ptr, style.ordinal)
        return this
    }

    /**
     * Set paint's mode to STROKE if true, or FILL if false.
     *
     * @param value  stroke or fill
     * @return       this
     */
    fun setStroke(value: Boolean): Paint {
        mode = (if (value) PaintMode.STROKE else PaintMode.FILL)
        return this
    }

    /**
     * Sets alpha and RGB used when stroking and filling. The color is a 32-bit value,
     * unpremultiplied, packing 8-bit components for alpha, red, blue, and green.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setColor](https://fiddle.skia.org/c/@Paint_setColor)
     *
     * Retrieves alpha and RGB, unpremultiplied, packed into 32 bits.
     * Use helpers [Color.getA], [Color.getR], [Color.getG], and [Color.getB] to extract
     * a color component.
     *
     * @return  unpremultiplied ARGB
     */
    var color: Int
        get() = try {
            Stats.onNativeCall()
            _nGetColor(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColor(value)
        }

    /**
     * Sets alpha and RGB used when stroking and filling. The color is a 32-bit value,
     * unpremultiplied, packing 8-bit components for alpha, red, blue, and green.
     *
     * @param color  unpremultiplied ARGB
     *
     * @see [https://fiddle.skia.org/c/@Paint_setColor](https://fiddle.skia.org/c/@Paint_setColor)
     */
    fun setColor(color: Int): Paint {
        Stats.onNativeCall()
        _nSetColor(_ptr, color)
        return this
    }

    /**
     * Sets alpha and RGB used when stroking and filling. The color is four floating
     * point values, unpremultiplied. The color values are interpreted as being in sRGB.
     *
     * Retrieves alpha and RGB, unpremultiplied, as four floating point values. RGB are
     * extended sRGB values (sRGB gamut, and encoded with the sRGB transfer function).
     *
     * @return  unpremultiplied RGBA
     */
    var color4f: Color4f
        get() = try {
            Stats.onNativeCall()
            _nGetColor4f(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColor4f(value)
        }
    
    /**
     * Sets alpha and RGB used when stroking and filling. The color is four floating
     * point values, unpremultiplied. The color values are interpreted as being in sRGB.
     *
     * @param color       unpremultiplied RGBA
     * @return            this
     */
    fun setColor4f(color: Color4f): Paint {
        return setColor4f(color, null)
    }

    /**
     * Sets alpha and RGB used when stroking and filling. The color is four floating
     * point values, unpremultiplied. The color values are interpreted as being in
     * the colorSpace. If colorSpace is nullptr, then color is assumed to be in the
     * sRGB color space.
     *
     * @param color       unpremultiplied RGBA
     * @param colorSpace  SkColorSpace describing the encoding of color
     * @return            this
     */
    fun setColor4f(color: Color4f, colorSpace: ColorSpace?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetColor4f(
                _ptr,
                color.r,
                color.g,
                color.b,
                color.a,
                getPtr(colorSpace)
            )
            this
        } finally {
            reachabilityBarrier(colorSpace)
        }
    }

    /**
     * Retrieves alpha from the color used when stroking and filling.
     *
     * @return  alpha ranging from 0f, fully transparent, to 1f, fully opaque
     */
    val alphaf: Float
        get() = color4f.a

    /**
     * Retrieves alpha from the color used when stroking and filling.
     *
     * @return  alpha ranging from 0, fully transparent, to 255, fully opaque
     */
    val alpha: Int
        get() = round(alphaf * 255f).toInt()

    /**
     *
     * Replaces alpha, leaving RGB unchanged. An out of range value triggers
     * an assert in the debug build. a is a value from 0f to 1f.
     *
     *
     * a set to zero makes color fully transparent; a set to 1.0 makes color
     * fully opaque.
     *
     * @param a  alpha component of color
     * @return   this
     */
    fun setAlphaf(a: Float): Paint {
        setColor4f(color4f.withA(a), null)
        return this
    }

    /**
     *
     * Replaces alpha, leaving RGB unchanged. An out of range value triggers
     * an assert in the debug build. a is a value from 0 to 255.
     *
     *
     * a set to zero makes color fully transparent; a set to 255 makes color
     * fully opaque.
     *
     * @param a  alpha component of color
     * @return   this
     */
    fun setAlpha(a: Int): Paint {
        return setAlphaf(a / 255f)
    }

    /**
     * Sets color used when drawing solid fills. The color components range from 0 to 255.
     * The color is unpremultiplied; alpha sets the transparency independent of RGB.
     *
     * @param a  amount of alpha, from fully transparent (0) to fully opaque (255)
     * @param r  amount of red, from no red (0) to full red (255)
     * @param g  amount of green, from no green (0) to full green (255)
     * @param b  amount of blue, from no blue (0) to full blue (255)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setARGB](https://fiddle.skia.org/c/@Paint_setARGB)
     */
    fun setARGB(a: Int, r: Int, g: Int, b: Int): Paint {
        Stats.onNativeCall()
        _nSetColor4f(_ptr, r / 255f, g / 255f, b / 255f, a / 255f, NULLPNTR)
        return this
    }

    /**
     * Sets the thickness of the pen used by the paint to outline the shape.
     * A stroke-width of zero is treated as "hairline" width. Hairlines are always exactly one
     * pixel wide in device space (their thickness does not change as the canvas is scaled).
     * Negative stroke-widths are invalid; setting a negative width will have no effect.
     *
     * @see [https://fiddle.skia.org/c/@Miter_Limit](https://fiddle.skia.org/c/@Miter_Limit)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeWidth](https://fiddle.skia.org/c/@Paint_setStrokeWidth)
     *
     * Returns the thickness of the pen used by Paint to outline the shape.
     *
     * @return  zero for hairline, greater than zero for pen thickness
     */
    var strokeWidth: Float
        get() = try {
            Stats.onNativeCall()
            _nGetStrokeWidth(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setStrokeWidth(value)
        }

    /**
     * Sets the thickness of the pen used by the paint to outline the shape.
     * A stroke-width of zero is treated as "hairline" width. Hairlines are always exactly one
     * pixel wide in device space (their thickness does not change as the canvas is scaled).
     * Negative stroke-widths are invalid; setting a negative width will have no effect.
     *
     * @param width  zero thickness for hairline; greater than zero for pen thickness
     *
     * @see [https://fiddle.skia.org/c/@Miter_Limit](https://fiddle.skia.org/c/@Miter_Limit)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeWidth](https://fiddle.skia.org/c/@Paint_setStrokeWidth)
     */
    fun setStrokeWidth(width: Float): Paint {
        Stats.onNativeCall()
        _nSetStrokeWidth(_ptr, width)
        return this
    }

    /**
     * Sets the limit at which a sharp corner is drawn beveled.
     * Valid values are zero and greater.
     * Has no effect if miter is less than zero.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeMiter](https://fiddle.skia.org/c/@Paint_setStrokeMiter)
     *
     * Returns the limit at which a sharp corner is drawn beveled.
     *
     * @return  zero and greater miter limit
     */
    var strokeMiter: Float
        get() = try {
            Stats.onNativeCall()
            _nGetStrokeMiter(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setStrokeMiter(value)
        }

    /**
     * Sets the limit at which a sharp corner is drawn beveled.
     * Valid values are zero and greater.
     * Has no effect if miter is less than zero.
     *
     * @param miter  zero and greater miter limit
     * @return       this
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeMiter](https://fiddle.skia.org/c/@Paint_setStrokeMiter)
     */
    fun setStrokeMiter(miter: Float): Paint {
        Stats.onNativeCall()
        _nSetStrokeMiter(_ptr, miter)
        return this
    }

    /**
     * Sets the geometry drawn at the beginning and end of strokes.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeCap_a](https://fiddle.skia.org/c/@Paint_setStrokeCap_a)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeCap_b](https://fiddle.skia.org/c/@Paint_setStrokeCap_b)
     *
     * @return  the geometry drawn at the beginning and end of strokes.
     */
    var strokeCap: PaintStrokeCap
        get() = try {
            Stats.onNativeCall()
            PaintStrokeCap.values().get(_nGetStrokeCap(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setStrokeCap(value)
        }

    /**
     * Sets the geometry drawn at the beginning and end of strokes.
     *
     * @return  this
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeCap_a](https://fiddle.skia.org/c/@Paint_setStrokeCap_a)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeCap_b](https://fiddle.skia.org/c/@Paint_setStrokeCap_b)
     */
    fun setStrokeCap(cap: PaintStrokeCap): Paint {
        Stats.onNativeCall()
        _nSetStrokeCap(_ptr, cap.ordinal)
        return this
    }


    /**
     * Sets the geometry drawn at the corners of strokes.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeJoin](https://fiddle.skia.org/c/@Paint_setStrokeJoin)
     *
     * @return  the geometry drawn at the corners of strokes.
     */
    var strokeJoin: PaintStrokeJoin
        get() = try {
            Stats.onNativeCall()
            PaintStrokeJoin.values().get(_nGetStrokeJoin(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setStrokeJoin(value)
        }

    /**
     * Sets the geometry drawn at the corners of strokes.
     *
     * @return  this
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeJoin](https://fiddle.skia.org/c/@Paint_setStrokeJoin)
     */
    fun setStrokeJoin(join: PaintStrokeJoin): Paint {
        Stats.onNativeCall()
        _nSetStrokeJoin(_ptr, join.ordinal)
        return this
    }

    /**
     * Returns the filled equivalent of the stroked path.
     *
     * @param src       Path read to create a filled version
     * @return          resulting Path
     */
    fun getFillPath(src: Path): Path {
        return getFillPath(src, null, 1f)
    }

    /**
     * Returns the filled equivalent of the stroked path.
     *
     * @param src       Path read to create a filled version
     * @param cull      Optional limit passed to PathEffect
     * @param resScale  if &gt; 1, increase precision, else if (0 &lt; resScale &lt; 1) reduce precision
     * to favor speed and size
     * @return          resulting Path
     */
    fun getFillPath(src: Path, cull: Rect?, resScale: Float): Path {
        return try {
            Stats.onNativeCall()
            if (cull == null) org.jetbrains.skia.Path(
                _nGetFillPath(
                    _ptr,
                    getPtr(src),
                    resScale
                )
            ) else org.jetbrains.skia.Path(
                _nGetFillPathCull(
                    _ptr,
                    getPtr(src),
                    cull.left,
                    cull.top,
                    cull.right,
                    cull.bottom,
                    resScale
                )
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(src)
        }
    }

    /**
     * @param shader  how geometry is filled with color; if null, color is used instead
     *
     * @see [https://fiddle.skia.org/c/@Color_Filter_Methods](https://fiddle.skia.org/c/@Color_Filter_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setShader](https://fiddle.skia.org/c/@Paint_setShader)
     *
     * @return  [Shader] or null
     * @see [https://fiddle.skia.org/c/@Paint_refShader](https://fiddle.skia.org/c/@Paint_refShader)
     */
    var shader: Shader?
        get() = try {
            Stats.onNativeCall()
            val shaderPtr = _nGetShader(_ptr)
            if (shaderPtr == NULLPNTR) null else Shader(shaderPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setShader(value)
        }

    /**
     * @param shader  how geometry is filled with color; if null, color is used instead
     *
     * @see [https://fiddle.skia.org/c/@Color_Filter_Methods](https://fiddle.skia.org/c/@Color_Filter_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setShader](https://fiddle.skia.org/c/@Paint_setShader)
     */
    fun setShader(shader: Shader?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetShader(_ptr, getPtr(shader))
            this
        } finally {
            reachabilityBarrier(shader)
        }
    }

    /**
     * @param colorFilter [ColorFilter] to apply to subsequent draw
     *
     * @see [https://fiddle.skia.org/c/@Blend_Mode_Methods](https://fiddle.skia.org/c/@Blend_Mode_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setColorFilter](https://fiddle.skia.org/c/@Paint_setColorFilter)
     *
     * @return  [ColorFilter] or null
     * @see [https://fiddle.skia.org/c/@Paint_refColorFilter](https://fiddle.skia.org/c/@Paint_refColorFilter)
     */
    var colorFilter: ColorFilter?
        get() = try {
            Stats.onNativeCall()
            val colorFilterPtr = _nGetColorFilter(_ptr)
            if (colorFilterPtr == NULLPNTR) null else ColorFilter(colorFilterPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setColorFilter(value)
        }

    /**
     * @param colorFilter [ColorFilter] to apply to subsequent draw
     *
     * @see [https://fiddle.skia.org/c/@Blend_Mode_Methods](https://fiddle.skia.org/c/@Blend_Mode_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setColorFilter](https://fiddle.skia.org/c/@Paint_setColorFilter)
     */
    fun setColorFilter(colorFilter: ColorFilter?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetColorFilter(
                _ptr,
                getPtr(colorFilter)
            )
            this
        } finally {
            reachabilityBarrier(colorFilter)
        }
    }

    /**
     * Sets SkBlendMode to mode. Does not check for valid input.
     * BlendMode used to combine source color and destination.
     *
     * Returns BlendMode. By default, returns [BlendMode.SRC_OVER].
     *
     * @return  mode used to combine source color with destination color
     */
    var blendMode: BlendMode
        get() = try {
            Stats.onNativeCall()
            BlendMode.values().get(_nGetBlendMode(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setBlendMode(value)
        }

    /**
     * Sets SkBlendMode to mode. Does not check for valid input.
     *
     * @param mode  BlendMode used to combine source color and destination
     * @return      this
     */
    fun setBlendMode(mode: BlendMode): Paint {
        Stats.onNativeCall()
        _nSetBlendMode(_ptr, mode.ordinal)
        return this
    }

    /**
     * @return  true if BlendMode is BlendMode.SRC_OVER, the default.
     */
    val isSrcOver: Boolean
        get() = blendMode == BlendMode.SRC_OVER

    /**
     * Replace [Path] with a modification when drawn
     *
     * @see [https://fiddle.skia.org/c/@Mask_Filter_Methods](https://fiddle.skia.org/c/@Mask_Filter_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setPathEffect](https://fiddle.skia.org/c/@Paint_setPathEffect)
     *
     * @return  [PathEffect] or null
     * @see [https://fiddle.skia.org/c/@Paint_refPathEffect](https://fiddle.skia.org/c/@Paint_refPathEffect)
     */
    var pathEffect: PathEffect?
        get() = try {
            Stats.onNativeCall()
            val pathEffectPtr = _nGetPathEffect(_ptr)
            if (pathEffectPtr == NULLPNTR) null else PathEffect(pathEffectPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setPathEffect(value)
        }

    /**
     * @param p  replace [Path] with a modification when drawn
     *
     * @see [https://fiddle.skia.org/c/@Mask_Filter_Methods](https://fiddle.skia.org/c/@Mask_Filter_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setPathEffect](https://fiddle.skia.org/c/@Paint_setPathEffect)
     */
    fun setPathEffect(p: PathEffect?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetPathEffect(_ptr, getPtr(p))
            this
        } finally {
            reachabilityBarrier(p)
        }
    }


    /**
     * maskFilter  modifies clipping mask generated from drawn geometry
     *
     * @see [https://fiddle.skia.org/c/@Paint_setMaskFilter](https://fiddle.skia.org/c/@Paint_setMaskFilter)
     *
     * @see [https://fiddle.skia.org/c/@Typeface_Methods](https://fiddle.skia.org/c/@Typeface_Methods)
     *
     * @return  [MaskFilter] if previously set, null otherwise
     * @see [https://fiddle.skia.org/c/@Paint_refMaskFilter](https://fiddle.skia.org/c/@Paint_refMaskFilter)
     */
    var maskFilter: MaskFilter?
        get() = try {
            Stats.onNativeCall()
            val maskFilterPtr = _nGetMaskFilter(_ptr)
            if (maskFilterPtr == NULLPNTR) null else MaskFilter(maskFilterPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setMaskFilter(value)
        }
    
    /**
     * @param maskFilter  modifies clipping mask generated from drawn geometry
     * @return            this
     *
     * @see [https://fiddle.skia.org/c/@Paint_setMaskFilter](https://fiddle.skia.org/c/@Paint_setMaskFilter)
     *
     * @see [https://fiddle.skia.org/c/@Typeface_Methods](https://fiddle.skia.org/c/@Typeface_Methods)
     */
    fun setMaskFilter(maskFilter: MaskFilter?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetMaskFilter(
                _ptr,
                getPtr(maskFilter)
            )
            this
        } finally {
            reachabilityBarrier(maskFilter)
        }
    }

    /**
     * imageFilter  how SkImage is sampled when transformed
     *
     * @see [https://fiddle.skia.org/c/@Draw_Looper_Methods](https://fiddle.skia.org/c/@Draw_Looper_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setImageFilter](https://fiddle.skia.org/c/@Paint_setImageFilter)
     *
     * @return  [ImageFilter] or null
     * @see [https://fiddle.skia.org/c/@Paint_refImageFilter](https://fiddle.skia.org/c/@Paint_refImageFilter)
     */
    var imageFilter: ImageFilter?
        get() = try {
            Stats.onNativeCall()
            val imageFilterPtr = _nGetImageFilter(_ptr)
            if (imageFilterPtr == NULLPNTR) null else org.jetbrains.skia.ImageFilter(imageFilterPtr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            setImageFilter(value)
        }
    
    /**
     * @param imageFilter  how SkImage is sampled when transformed
     *
     * @see [https://fiddle.skia.org/c/@Draw_Looper_Methods](https://fiddle.skia.org/c/@Draw_Looper_Methods)
     *
     * @see [https://fiddle.skia.org/c/@Paint_setImageFilter](https://fiddle.skia.org/c/@Paint_setImageFilter)
     */
    fun setImageFilter(imageFilter: ImageFilter?): Paint {
        return try {
            Stats.onNativeCall()
            _nSetImageFilter(
                _ptr,
                getPtr(imageFilter)
            )
            this
        } finally {
            reachabilityBarrier(imageFilter)
        }
    }

    /**
     *
     * Returns true if Paint prevents all drawing;
     * otherwise, the Paint may or may not allow drawing.
     *
     *
     * Returns true if, for example, BlendMode combined with alpha computes a
     * new alpha of zero.
     *
     * @return  true if Paint prevents all drawing
     *
     * @see [https://fiddle.skia.org/c/@Paint_nothingToDraw](https://fiddle.skia.org/c/@Paint_nothingToDraw)
     */
    fun hasNothingToDraw(): Boolean {
        return try {
            Stats.onNativeCall()
            _nHasNothingToDraw(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }
}