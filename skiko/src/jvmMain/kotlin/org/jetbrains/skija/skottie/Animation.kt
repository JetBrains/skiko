package org.jetbrains.skija.skottie

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Managed.CleanerThunk
import org.jetbrains.skija.paragraph.Shadow
import org.jetbrains.skija.paragraph.TextBox
import org.jetbrains.skija.paragraph.Affinity
import org.jetbrains.skija.paragraph.Paragraph
import org.jetbrains.skija.paragraph.HeightMode
import org.jetbrains.skija.paragraph.StrutStyle
import org.jetbrains.skija.paragraph.BaselineMode
import org.jetbrains.skija.paragraph.RectWidthMode
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.paragraph.ParagraphCache
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.RectHeightMode
import org.jetbrains.skija.paragraph.DecorationStyle
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.PlaceholderStyle
import org.jetbrains.skija.paragraph.TextStyleAttribute
import org.jetbrains.skija.paragraph.DecorationLineStyle
import org.jetbrains.skija.paragraph.PlaceholderAlignment
import org.jetbrains.skija.paragraph.PositionWithAffinity
import org.jetbrains.skija.paragraph.TypefaceFontProvider
import org.jetbrains.skija.shaper.Shaper
import org.jetbrains.skija.shaper.FontRun
import org.jetbrains.skija.shaper.LanguageRun
import org.jetbrains.skija.shaper.ShapingOptions
import org.jetbrains.skija.shaper.FontMgrRunIterator
import org.jetbrains.skija.shaper.IcuBidiRunIterator
import org.jetbrains.skija.shaper.ManagedRunIterator
import org.jetbrains.skija.shaper.HbIcuScriptRunIterator
import org.jetbrains.skija.shaper.TextBlobBuilderRunHandler
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import org.jetbrains.skija.skottie.Animation
import org.jetbrains.skija.sksg.InvalidationController
import org.jetbrains.skija.skottie.RenderFlag
import org.jetbrains.skija.skottie.AnimationBuilder
import org.jetbrains.skija.skottie.AnimationBuilderFlag
import org.jetbrains.skija.svg.SVGDOM
import org.jetbrains.skija.svg.SVGSVG
import org.jetbrains.skija.svg.SVGTag
import org.jetbrains.skija.svg.SVGNode
import org.jetbrains.skija.svg.SVGCanvas
import org.jetbrains.skija.svg.SVGLength
import org.jetbrains.skija.svg.SVGLengthType
import org.jetbrains.skija.svg.SVGLengthUnit
import org.jetbrains.skija.svg.SVGLengthContext
import org.jetbrains.skija.svg.SVGPreserveAspectRatio
import org.jetbrains.skija.svg.SVGPreserveAspectRatioAlign
import org.jetbrains.skija.svg.SVGPreserveAspectRatioScale
import org.jetbrains.skija.ColorFilter._LinearToSRGBGammaHolder
import org.jetbrains.skija.ColorFilter._SRGBToLinearGammaHolder
import org.jetbrains.skija.ColorFilter._LumaHolder
import org.jetbrains.skija.ColorSpace._SRGBHolder
import org.jetbrains.skija.ColorSpace._SRGBLinearHolder
import org.jetbrains.skija.ColorSpace._DisplayP3Holder
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.Contract
import org.jetbrains.skija.*
import org.jetbrains.skija.FontMgr._DefaultHolder
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class Animation @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @Contract("!null -> new; null -> fail")
        fun makeFromString(data: String): Animation {
            assert(data != null) { "Can’t Animation::makeFromString with data == null" }
            Stats.onNativeCall()
            val ptr = _nMakeFromString(data)
            require(ptr != 0L) { "Failed to create Animation from string=\"$data\"" }
            return Animation(ptr)
        }

        @Contract("!null -> new; null -> fail")
        fun makeFromFile(path: String): Animation {
            assert(path != null) { "Can’t Animation::makeFromFile with path == null" }
            Stats.onNativeCall()
            val ptr = _nMakeFromFile(path)
            require(ptr != 0L) { "Failed to create Animation from path=\"$path\"" }
            return Animation(ptr)
        }

        @Contract("!null -> new; null -> fail")
        fun makeFromData(data: Data): Animation {
            assert(data != null) { "Can’t Animation::makeFromData with data == null" }
            Stats.onNativeCall()
            val ptr = _nMakeFromData(Native.Companion.getPtr(data))
            require(ptr != 0L) { "Failed to create Animation from data." }
            return Animation(ptr)
        }

        @ApiStatus.Internal
        external fun _nGetFinalizer(): Long
        @ApiStatus.Internal
        external fun _nMakeFromString(data: String?): Long
        @ApiStatus.Internal
        external fun _nMakeFromFile(path: String?): Long
        @ApiStatus.Internal
        external fun _nMakeFromData(dataPtr: Long): Long
        @ApiStatus.Internal
        external fun _nRender(
            ptr: Long,
            canvasPtr: Long,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            flags: Int
        )

        @ApiStatus.Internal
        external fun _nSeek(ptr: Long, t: Float, icPtr: Long)
        @ApiStatus.Internal
        external fun _nSeekFrame(ptr: Long, t: Float, icPtr: Long)
        @ApiStatus.Internal
        external fun _nSeekFrameTime(ptr: Long, t: Float, icPtr: Long)
        @ApiStatus.Internal
        external fun _nGetDuration(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetFPS(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetInPoint(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetOutPoint(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetVersion(ptr: Long): String
        @ApiStatus.Internal
        external fun _nGetSize(ptr: Long): Point?

        init {
            staticLoad()
        }
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    /**
     *
     * Draws the current animation frame
     *
     *
     * It is undefined behavior to call render() on a newly created Animation
     * before specifying an initial frame via one of the seek() variants.
     *
     * @param canvas  destination canvas
     * @return        this
     */
    @Contract("!null -> this; null -> fail")
    fun render(canvas: Canvas): Animation {
        return render(canvas, Rect.Companion.makeXYWH(0f, 0f, width, height))
    }

    /**
     *
     * Draws the current animation frame
     *
     *
     * It is undefined behavior to call render() on a newly created Animation
     * before specifying an initial frame via one of the seek() variants.
     *
     * @param canvas  destination canvas
     * @param offset  destination offset
     * @return        this
     */
    @Contract("_, _, _ -> this")
    fun render(canvas: Canvas, offset: Point): Animation {
        assert(offset != null) { "Can’t Animation::render with offset == null" }
        return render(canvas, offset._x, offset._y)
    }

    /**
     *
     * Draws the current animation frame
     *
     *
     * It is undefined behavior to call render() on a newly created Animation
     * before specifying an initial frame via one of the seek() variants.
     *
     * @param canvas  destination canvas
     * @param left    destination offset left
     * @param top     destination offset top
     * @return        this
     */
    @Contract("_, _, _ -> this")
    fun render(canvas: Canvas, left: Float, top: Float): Animation {
        return render(canvas, Rect.Companion.makeXYWH(left, top, width, height))
    }

    /**
     *
     * Draws the current animation frame
     *
     *
     * It is undefined behavior to call render() on a newly created Animation
     * before specifying an initial frame via one of the seek() variants.
     *
     * @param canvas       destination canvas
     * @param dst          destination rect
     * @param renderFlags  render flags
     * @return             this
     */
    @Contract("_, _, _ -> this")
    fun render(canvas: Canvas, dst: Rect, vararg renderFlags: RenderFlag): Animation {
        return try {
            assert(canvas != null) { "Can’t Animation::render with canvas == null" }
            assert(dst != null) { "Can’t Animation::render with dst == null" }
            Stats.onNativeCall()
            var flags = 0
            for (flag in renderFlags) flags = flags or flag._flag
            _nRender(_ptr, Native.Companion.getPtr(canvas), dst._left, dst._top, dst._right, dst._bottom, flags)
            this
        } finally {
            Reference.reachabilityFence(canvas)
        }
    }

    /**
     *
     * Updates the animation state for |t|.
     *
     * @param t   normalized [0..1] frame selector (0 → first frame, 1 → final frame)
     * @return    this
     */
    @Contract("_ -> this")
    fun seek(t: Float): Animation {
        return seek(t, null)
    }

    /**
     *
     * Updates the animation state for |t|.
     *
     * @param t   normalized [0..1] frame selector (0 → first frame, 1 → final frame)
     * @param ic  invalidation controller (dirty region tracking)
     * @return    this
     */
    @Contract("_, _ -> this")
    fun seek(t: Float, ic: InvalidationController?): Animation {
        return try {
            Stats.onNativeCall()
            _nSeek(_ptr, t, Native.Companion.getPtr(ic))
            this
        } finally {
            Reference.reachabilityFence(ic)
        }
    }

    /**
     *
     * Update the animation state to match |t|, specified as a frame index i.e.
     * relative to [] * [].
     *
     *
     * Fractional values are allowed and meaningful - e.g.
     * 0.0 → first frame 1.0 → second frame 0.5 → halfway between first and second frame
     *
     * @param t   frame index
     * @return    this
     */
    @Contract("_ -> this")
    fun seekFrame(t: Float): Animation {
        return seekFrame(t, null)
    }

    /**
     *
     * Update the animation state to match |t|, specified as a frame index i.e.
     * relative to [] * [].
     *
     *
     * Fractional values are allowed and meaningful - e.g.
     * 0.0 → first frame 1.0 → second frame 0.5 → halfway between first and second frame
     *
     * @param t   frame index
     * @param ic  invalidation controller (dirty region tracking)
     * @return    this
     */
    @Contract("_, _ -> this")
    fun seekFrame(t: Float, ic: InvalidationController?): Animation {
        return try {
            Stats.onNativeCall()
            _nSeekFrame(_ptr, t, Native.Companion.getPtr(ic))
            this
        } finally {
            Reference.reachabilityFence(ic)
        }
    }

    /**
     *
     * Update the animation state to match t, specifed in frame time i.e.
     * relative to [].
     *
     * @param t   frame time
     * @return    this
     */
    @Contract("_ -> this")
    fun seekFrameTime(t: Float): Animation {
        return seekFrameTime(t, null)
    }

    /**
     *
     * Update the animation state to match t, specifed in frame time i.e.
     * relative to [].
     *
     * @param t   frame time
     * @param ic  invalidation controller (dirty region tracking)
     * @return    this
     */
    @Contract("_, _ -> this")
    fun seekFrameTime(t: Float, ic: InvalidationController?): Animation {
        return try {
            Stats.onNativeCall()
            _nSeekFrameTime(_ptr, t, Native.Companion.getPtr(ic))
            this
        } finally {
            Reference.reachabilityFence(ic)
        }
    }

    /**
     * @return  the animation duration in seconds
     */
    val duration: Float
        get() = try {
            Stats.onNativeCall()
            _nGetDuration(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  the animation frame rate (frames / second)
     */
    val fPS: Float
        get() = try {
            Stats.onNativeCall()
            _nGetFPS(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  Animation in point, in frame index units
     */
    val inPoint: Float
        get() = try {
            Stats.onNativeCall()
            _nGetInPoint(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  Animation out point, in frame index units
     */
    val outPoint: Float
        get() = try {
            Stats.onNativeCall()
            _nGetOutPoint(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val version: String
        get() = try {
            Stats.onNativeCall()
            _nGetVersion(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    @ApiStatus.Internal
    var _size: Point? = null
    val size: Point
        get() {
            if (_size == null) {
                _size = _nGetSize(_ptr)
            }
            return _size!!
        }
    val width: Float
        get() = size._x
    val height: Float
        get() = size._y
}