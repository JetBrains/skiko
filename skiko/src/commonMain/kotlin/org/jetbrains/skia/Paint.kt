package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import kotlin.math.round

class Paint : Managed {
    companion object {
        init {
            staticLoad()
        }
    }

    internal object _FinalizerHolder {
        val PTR = Paint_nGetFinalizer()
    }

    internal constructor(ptr: NativePointer, managed: Boolean) : super(ptr, _FinalizerHolder.PTR, managed)

    /**
     * Constructs SkPaint with default values.
     *
     * @see [https://fiddle.skia.org/c/@Paint_empty_constructor](https://fiddle.skia.org/c/@Paint_empty_constructor)
     */
    constructor() : super(Paint_nMake(), _FinalizerHolder.PTR) {
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
    fun makeClone(): Paint = nativeCall { Paint(Paint_nMakeClone(_ptr), true) }

    override fun nativeEquals(other: Native?): Boolean = nativeCall(other) { other ->
        Paint_nEquals(_ptr, other.pointerOrNullPointer)
    }

    /**
     * Sets all Paint contents to their initial values. This is equivalent to replacing
     * Paint with the result of Paint().
     *
     * @see [https://fiddle.skia.org/c/@Paint_reset](https://fiddle.skia.org/c/@Paint_reset)
     */
    fun reset(): Paint = nativeCall {
        Paint_nReset(_ptr)
        this
    }

    /**
     * Requests, but does not require, that edge pixels draw opaque or with partial transparency.
     *
     * Returns true if pixels on the active edges of Path may be drawn with partial transparency.
     *
     * @return  antialiasing state
     */
    var isAntiAlias: Boolean
        get() = nativeCall { _nIsAntiAlias(_ptr) }
        set(value) = nativeCall { _nSetAntiAlias(_ptr, value) }

    /**
     * Requests, but does not require, to distribute color error.
     *
     * @return  true if color error may be distributed to smooth color transition.
     */
    var isDither: Boolean
        get() = nativeCall { _nIsDither(_ptr) }
        set(value)  = nativeCall { _nSetDither(_ptr, value) }

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
        get() = nativeCall { PaintMode.entries[_nGetMode(_ptr)] }
        set(value) = nativeCall { _nSetMode(_ptr, value.ordinal) }

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
        get() = nativeCall { Paint_nGetColor(_ptr) }
        set(value) = nativeCall { _nSetColor(_ptr, value) }

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
        get() = nativeCall {
            Color4f(withResult(FloatArray(4)) {
                _nGetColor4f(_ptr, it)
            })
        }
        set(value) {
            setColor4f(value, null)
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
    fun setColor4f(color: Color4f, colorSpace: ColorSpace?): Paint = nativeCall(colorSpace) { colorSpace ->
        _nSetColor4f(
            _ptr,
            color.r,
            color.g,
            color.b,
            color.a,
            colorSpace.pointerOrNullPointer
        )
        this
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
    var alpha: Int
        get() = round(alphaf * 255f).toInt()
        set(value) { setAlphaf(value / 255f) }

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
    fun setARGB(a: Int, r: Int, g: Int, b: Int): Paint = nativeCall {
        _nSetColor4f(_ptr, r / 255f, g / 255f, b / 255f, a / 255f, NullPointer)
        this
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
        get() = nativeCall { _nGetStrokeWidth(_ptr) }
        set(value) = nativeCall { _nSetStrokeWidth(_ptr, value) }


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
        get() = nativeCall { _nGetStrokeMiter(_ptr) }
        set(value) = nativeCall { _nSetStrokeMiter(_ptr, value) }


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
        get() = nativeCall { PaintStrokeCap.entries[_nGetStrokeCap(_ptr)] }
        set(value) = nativeCall { _nSetStrokeCap(_ptr, value.ordinal) }

    /**
     * Sets the geometry drawn at the corners of strokes.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setStrokeJoin](https://fiddle.skia.org/c/@Paint_setStrokeJoin)
     *
     * @return  the geometry drawn at the corners of strokes.
     */
    var strokeJoin: PaintStrokeJoin
        get() = nativeCall { PaintStrokeJoin.entries[_nGetStrokeJoin(_ptr)] }
        set(value) = nativeCall { _nSetStrokeJoin(_ptr, value.ordinal) }

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
        get() = nativeCall {
            val shaderPtr = _nGetShader(_ptr)
            if (shaderPtr == NullPointer) null else Shader(shaderPtr)
        }
        set(value) = nativeCall(value) { value ->
            _nSetShader(_ptr, value.pointerOrNullPointer)
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
        get() = nativeCall {
            val colorFilterPtr = _nGetColorFilter(_ptr)
            if (colorFilterPtr == NullPointer) null else ColorFilter(colorFilterPtr)
        }
        set(value) = nativeCall(value) { value ->
            _nSetColorFilter(_ptr, value.pointerOrNullPointer)
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
        get() = nativeCall { BlendMode.entries[_nGetBlendMode(_ptr)] }
        set(value) = nativeCall { _nSetBlendMode(_ptr, value.ordinal) }

    /**
     * Returns the user-supplied blend function, if one has been set.
     *
     * A null blender signifies the default SrcOver behavior.
     *
     * For convenience, you can call [blendMode] if the blend effect can be expressed
     * as one of those values.
     *
     * @see [https://fiddle.skia.org/c/@Paint_setBlender](https://fiddle.skia.org/c/@Paint_setBlender)
     * @see [https://fiddle.skia.org/c/@Paint_refBlender](https://fiddle.skia.org/c/@Paint_refBlender)
     *
     * @return  the [Blender] assigned to this paint, otherwise null
     */
    var blender: Blender?
        get() = nativeCall {
            val blenderPtr = _nGetBlender(_ptr)
            if (blenderPtr == NullPointer) null else Blender(blenderPtr)
        }
        set(value) = nativeCall(value) { value ->
            _nSetBlender(_ptr, value.pointerOrNullPointer)
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
        get() = nativeCall {
            val pathEffectPtr = _nGetPathEffect(_ptr)
            if (pathEffectPtr == NullPointer) null else PathEffect(pathEffectPtr)
        }
        set(value) = nativeCall(value) { value ->
            _nSetPathEffect(_ptr, value.pointerOrNullPointer)
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
        get() = nativeCall {
            val maskFilterPtr = _nGetMaskFilter(_ptr)
            if (maskFilterPtr == NullPointer) null else MaskFilter(maskFilterPtr)
        }
        set(value) = nativeCall(value) { value ->
            _nSetMaskFilter(_ptr, value.pointerOrNullPointer)
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
        get() = nativeCall {
            val imageFilterPtr = _nGetImageFilter(_ptr)
            if (imageFilterPtr == NullPointer) null else ImageFilter(imageFilterPtr)
        }
        set(value) = nativeCall(value) { value ->
            _nSetImageFilter(_ptr, value.pointerOrNullPointer)
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
    fun hasNothingToDraw(): Boolean = nativeCall { _nHasNothingToDraw(_ptr) }
}

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of the paint cleanup function without running it, so it is short and
// cannot block.
// Native call path: deletePaint
private external fun Paint_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nMake")
// No ForceNativeThreadState: creates a paint that holds only plain values and no object references, then turns on
// anti-aliasing, so it is short and cannot block.
// Native call path: SkPaint::SkPaint -> SkPaint::setAntiAlias
private external fun Paint_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nMakeClone")
// No ForceNativeThreadState: copies a paint's values and adds a reference to each object it holds, so it is short and
// cannot block.
// Native call path: SkPaint::SkPaint
private external fun Paint_nMakeClone(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nEquals")
// No ForceNativeThreadState: compares a paint's stored values and object references with another paint's, so it is
// short and cannot block.
// Native call path: SkPaint::operator==
private external fun Paint_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Paint__1nReset")
// Needs ForceNativeThreadState: clears the paint, which can free its shader or image and release a Metal GPU texture.
// Native call path: SkPaint::reset -> SkPaint::operator= -> ~SkImage_Ganesh -> GrMtlAttachment::onRelease
@ForceNativeThreadState
private external fun Paint_nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nIsAntiAlias")
// No ForceNativeThreadState: reads a stored on/off flag, so it is short and cannot block.
// Native call path: SkPaint::isAntiAlias
private external fun _nIsAntiAlias(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetAntiAlias")
// No ForceNativeThreadState: sets a stored on/off flag, so it is short and cannot block.
// Native call path: SkPaint::setAntiAlias
private external fun _nSetAntiAlias(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nIsDither")
// No ForceNativeThreadState: reads a stored on/off flag, so it is short and cannot block.
// Native call path: SkPaint::isDither
private external fun _nIsDither(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetDither")
// No ForceNativeThreadState: sets a stored on/off flag, so it is short and cannot block.
// Native call path: SkPaint::setDither
private external fun _nSetDither(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetMode")
// No ForceNativeThreadState: reads the stored paint style, so it is short and cannot block.
// Native call path: SkPaint::getStyle
private external fun _nGetMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetMode")
// No ForceNativeThreadState: checks the style value's range and stores it, so it is short and cannot block.
// Native call path: SkPaint::setStyle
private external fun _nSetMode(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetColor")
// No ForceNativeThreadState: converts the stored float color to an integer color, so it is short and cannot block.
// Native call path: SkPaint::getColor
private external fun Paint_nGetColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetColor4f")
// No ForceNativeThreadState: copies the four stored float color values into the result, so it is short and cannot
// block.
// Native call path: SkPaint::getColor4f
private external fun _nGetColor4f(ptr: NativePointer, arr: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetColor")
// No ForceNativeThreadState: converts the color to float form and stores it, so it is short and cannot block.
// Native call path: SkPaint::setColor -> SkColor4f::FromColor
private external fun _nSetColor(ptr: NativePointer, argb: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetColor4f")
// Needs ForceNativeThreadState: for a color that is not in the sRGB color space, it can wait for another thread that is
// still setting up shared color data.
// Native call path: SkPaint::setColor4f -> SkColorSpace::invTransferFn -> SkOnce::operator()
@ForceNativeThreadState
private external fun _nSetColor4f(ptr: NativePointer, r: Float, g: Float, b: Float, a: Float, colorSpacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeWidth")
// No ForceNativeThreadState: returns the stored stroke width, so it is short and cannot block.
// Native call path: SkPaint::getStrokeWidth
private external fun _nGetStrokeWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeWidth")
// No ForceNativeThreadState: stores a new stroke width after checking it is not negative, so it is short and cannot
// block.
// Native call path: SkPaint::setStrokeWidth
private external fun _nSetStrokeWidth(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeMiter")
// No ForceNativeThreadState: returns the stored miter limit, so it is short and cannot block.
// Native call path: SkPaint::getStrokeMiter
private external fun _nGetStrokeMiter(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeMiter")
// No ForceNativeThreadState: stores a new miter limit after checking it is not negative, so it is short and cannot
// block.
// Native call path: SkPaint::setStrokeMiter
private external fun _nSetStrokeMiter(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeCap")
// No ForceNativeThreadState: reads the stored stroke cap style, so it is short and cannot block.
// Native call path: SkPaint::getStrokeCap
private external fun _nGetStrokeCap(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeCap")
// No ForceNativeThreadState: checks the cap style's range and stores it, so it is short and cannot block.
// Native call path: SkPaint::setStrokeCap
private external fun _nSetStrokeCap(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeJoin")
// No ForceNativeThreadState: returns the stored stroke join style, so it is short and cannot block.
// Native call path: SkPaint::getStrokeJoin
private external fun _nGetStrokeJoin(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeJoin")
// No ForceNativeThreadState: checks the join value is in range and stores it in a flag field, so it is short and cannot
// block.
// Native call path: SkPaint::setStrokeJoin
private external fun _nSetStrokeJoin(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetShader")
// No ForceNativeThreadState: returns the shader with an extra reference count, so it is short and cannot block.
// Native call path: SkPaint::refShader
private external fun _nGetShader(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetShader")
// Needs ForceNativeThreadState: can drop the last reference to an old image shader, which can free a Metal GPU texture.
// Native call path: SkPaint::setShader -> ~SkImageShader -> ~SkImage_Ganesh -> GrMtlAttachment::onRelease
@ForceNativeThreadState
private external fun _nSetShader(ptr: NativePointer, shaderPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetColorFilter")
// No ForceNativeThreadState: returns the color filter with an extra reference count, so it is short and cannot block.
// Native call path: SkPaint::refColorFilter
private external fun _nGetColorFilter(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetColorFilter")
// Needs ForceNativeThreadState: can drop the last reference to an old runtime color filter, whose child image shader
// can free a Metal GPU texture.
// Native call path: SkPaint::setColorFilter -> ~SkRuntimeColorFilter -> ~SkImageShader -> GrMtlAttachment::onRelease
@ForceNativeThreadState
private external fun _nSetColorFilter(ptr: NativePointer, colorFilterPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetBlendMode")
// No ForceNativeThreadState: reads the stored blend mode or returns a fallback when the blender has no fixed mode, so
// it is short and cannot block.
// Native call path: SkPaint::getBlendMode_or -> SkBlendModeBlender::asBlendMode
private external fun _nGetBlendMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetBlendMode")
// Needs ForceNativeThreadState: replacing a runtime blender can drop the last reference to its child image shader,
// which can free a Metal GPU texture.
// Native call path: SkPaint::setBlendMode -> ~SkRuntimeBlender -> ~SkImageShader -> GrMtlAttachment::onRelease
@ForceNativeThreadState
private external fun _nSetBlendMode(ptr: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetPathEffect")
// No ForceNativeThreadState: returns the path effect with an extra reference count, so it is short and cannot block.
// Native call path: SkPaint::refPathEffect
private external fun _nGetPathEffect(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetPathEffect")
// Needs ForceNativeThreadState: can free the last reference to an old path effect tree, and freeing it visits every
// node, so its time grows with the tree size.
// Native call path: SkPaint::setPathEffect -> sk_sp::reset -> ~SkPairPathEffect
@ForceNativeThreadState
private external fun _nSetPathEffect(ptr: NativePointer, pathEffectPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetMaskFilter")
// No ForceNativeThreadState: returns the mask filter with an extra reference count, so it is short and cannot block.
// Native call path: SkPaint::refMaskFilter
private external fun _nGetMaskFilter(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetMaskFilter")
// Needs ForceNativeThreadState: can drop the last reference to an old mask filter that holds an image shader, which can
// free a Metal GPU texture.
// Native call path: SkPaint::setMaskFilter -> ~SkShaderMaskFilterImpl -> ~SkImageShader -> GrMtlAttachment::onRelease
@ForceNativeThreadState
private external fun _nSetMaskFilter(ptr: NativePointer, filterPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetImageFilter")
// No ForceNativeThreadState: returns the image filter with an extra reference count, so it is short and cannot block.
// Native call path: SkPaint::refImageFilter
private external fun _nGetImageFilter(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetImageFilter")
// Needs ForceNativeThreadState: can drop the last reference to an old image filter's image, which can free a Metal GPU
// texture.
// Native call path: SkPaint::setImageFilter -> ~SkImageImageFilter -> ~SkImage_Ganesh -> GrMtlAttachment::onRelease
@ForceNativeThreadState
private external fun _nSetImageFilter(ptr: NativePointer, filterPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetBlender")
// No ForceNativeThreadState: returns the blender with an extra reference count, so it is short and cannot block.
// Native call path: SkPaint::refBlender
private external fun _nGetBlender(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetBlender")
// Needs ForceNativeThreadState: can drop the last reference to an old runtime blender, whose child image shader can
// free a Metal GPU texture.
// Native call path: SkPaint::setBlender -> ~SkRuntimeBlender -> ~SkImageShader -> GrMtlAttachment::onRelease
@ForceNativeThreadState
private external fun _nSetBlender(ptr: NativePointer, blenderPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nHasNothingToDraw")
// Needs ForceNativeThreadState: with zero opacity and source-over blending, recursively checks nested color filters, so
// its time grows with the filter tree size.
// Native call path: SkPaint::nothingToDraw -> SkComposeColorFilter::onIsAlphaUnchanged
@ForceNativeThreadState
private external fun _nHasNothingToDraw(ptr: NativePointer): Boolean
