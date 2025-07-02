@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.skottie

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Data
import org.jetbrains.skia.Point
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withStringReferenceResult
import org.jetbrains.skia.sksg.InvalidationController

class Animation internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun makeFromString(data: String): Animation {
            Stats.onNativeCall()
            interopScope {
                val ptr = Animation_nMakeFromString(toInterop(data))
                require(ptr != NullPointer) { "Failed to create Animation from string=\"$data\"" }
                return Animation(ptr)
            }
        }

        fun makeFromData(data: Data): Animation {
            Stats.onNativeCall()
            val ptr = try {
                Animation_nMakeFromData(getPtr(data))
            } finally {
                reachabilityBarrier(data)
            }
            require(ptr != NullPointer) { "Failed to create Animation from data." }
            return Animation(ptr)
        }

        init {
            staticLoad()
        }
    }

    internal object _FinalizerHolder {
        val PTR = Animation_nGetFinalizer()
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
            Stats.onNativeCall()
            var flags = 0
            for (flag in renderFlags) flags = flags or flag._flag
            Animation_nRender(_ptr, getPtr(canvas), dst.left, dst.top, dst.right, dst.bottom, flags)
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(canvas)
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
            Animation_nSeek(_ptr, t, getPtr(ic))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(ic)
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
            Animation_nSeekFrame(_ptr, t, getPtr(ic))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(ic)
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
            Animation_nSeekFrameTime(_ptr, t, getPtr(ic))
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(ic)
        }
    }

    /**
     * @return  the animation duration in seconds
     */
    val duration: Float
        get() = try {
            Stats.onNativeCall()
            Animation_nGetDuration(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  the animation frame rate (frames / second)
     */
    val fPS: Float
        get() = try {
            Stats.onNativeCall()
            Animation_nGetFPS(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  Animation in point, in frame index units
     */
    val inPoint: Float
        get() = try {
            Stats.onNativeCall()
            Animation_nGetInPoint(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  Animation out point, in frame index units
     */
    val outPoint: Float
        get() = try {
            Stats.onNativeCall()
            Animation_nGetOutPoint(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val version: String
        get() = try {
            Stats.onNativeCall()
            withStringReferenceResult { Animation_nGetVersion(_ptr) }
        } finally {
            reachabilityBarrier(this)
        }

    private var _size: Point? = null
    val size: Point
        get() {
            if (_size == null) {
                _size = Point.fromInteropPointer { Animation_nGetSize(_ptr, it) }
            }
            return _size!!
        }
    val width: Float
        get() = size.x
    val height: Float
        get() = size.y
}