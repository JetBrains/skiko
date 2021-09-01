package org.jetbrains.skija.skottie

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.sksg.InvalidationController
import org.jetbrains.skija.*
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class Animation internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun makeFromString(data: String): Animation {
            Stats.onNativeCall()
            val ptr = _nMakeFromString(data)
            require(ptr != 0L) { "Failed to create Animation from string=\"$data\"" }
            return Animation(ptr)
        }

        fun makeFromFile(path: String): Animation {
            Stats.onNativeCall()
            val ptr = _nMakeFromFile(path)
            require(ptr != 0L) { "Failed to create Animation from path=\"$path\"" }
            return Animation(ptr)
        }

        fun makeFromData(data: Data): Animation {
            Stats.onNativeCall()
            val ptr = _nMakeFromData(Native.Companion.getPtr(data))
            require(ptr != 0L) { "Failed to create Animation from data." }
            return Animation(ptr)
        }

        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMakeFromString(data: String?): Long
        @JvmStatic external fun _nMakeFromFile(path: String?): Long
        @JvmStatic external fun _nMakeFromData(dataPtr: Long): Long
        @JvmStatic external fun _nRender(
            ptr: Long,
            canvasPtr: Long,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            flags: Int
        )

        @JvmStatic external fun _nSeek(ptr: Long, t: Float, icPtr: Long)
        @JvmStatic external fun _nSeekFrame(ptr: Long, t: Float, icPtr: Long)
        @JvmStatic external fun _nSeekFrameTime(ptr: Long, t: Float, icPtr: Long)
        @JvmStatic external fun _nGetDuration(ptr: Long): Float
        @JvmStatic external fun _nGetFPS(ptr: Long): Float
        @JvmStatic external fun _nGetInPoint(ptr: Long): Float
        @JvmStatic external fun _nGetOutPoint(ptr: Long): Float
        @JvmStatic external fun _nGetVersion(ptr: Long): String
        @JvmStatic external fun _nGetSize(ptr: Long): Point?

        init {
            staticLoad()
        }
    }

    internal object _FinalizerHolder {
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
    fun render(canvas: Canvas, offset: Point): Animation {
        return render(canvas, offset.x, offset.y)
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
    fun render(canvas: Canvas, dst: Rect, vararg renderFlags: RenderFlag): Animation {
        return try {
            assert(canvas != null) { "Can’t Animation::render with canvas == null" }
            assert(dst != null) { "Can’t Animation::render with dst == null" }
            Stats.onNativeCall()
            var flags = 0
            for (flag in renderFlags) flags = flags or flag._flag
            _nRender(_ptr, Native.Companion.getPtr(canvas), dst.left, dst.top, dst.right, dst.bottom, flags)
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

    private var _size: Point? = null
    val size: Point
        get() {
            if (_size == null) {
                _size = _nGetSize(_ptr)
            }
            return _size!!
        }
    val width: Float
        get() = size.x
    val height: Float
        get() = size.y
}