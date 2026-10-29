package org.jetbrains.skia.skottie

import org.jetbrains.skia.sksg.InvalidationController
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.*
import kotlin.jvm.JvmStatic

class Animation internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        fun makeFromString(data: String): Animation = nativeCallFromStatic {
            interopScope {
                val ptr = _nMakeFromString(toInterop(data))
                require(ptr != NullPointer) { "Failed to create Animation from string=\"$data\"" }
                return Animation(ptr)
            }
        }

        @JvmStatic
        fun makeFromData(data: Data): Animation = nativeCallFromStatic(data) { data ->
            val ptr = _nMakeFromData(data.pointerOrNullPointer)
            require(ptr != NullPointer) { "Failed to create Animation from data." }
            Animation(ptr)
        }

        init {
            SkottieLibrary.load()
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
        return render(canvas, 0f, 0f, width, height)
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
        return render(canvas, offset.x, offset.y, offset.x + width, offset.y + height)
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
        return render(canvas, left, top, left + width, top + height)
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
        return render(canvas, dst.left, dst.top, dst.right, dst.bottom, renderFlags = renderFlags)
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
     * @param left         destination left
     * @param top          destination top
     * @param right        destination right
     * @param bottom       destination bottom
     * @param renderFlags  render flags
     * @return             this
     */
    fun render(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, vararg renderFlags: RenderFlag): Animation =
        nativeCall(canvas) { canvas ->
            var flags = 0
            for (flag in renderFlags) flags = flags or flag._flag
            _nRender(nativePtr, canvas.nativePtr, left, top, right, bottom, flags)
            this
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
    fun seek(t: Float, ic: InvalidationController?): Animation =
        nativeCall(ic) { ic ->
            _nSeek(nativePtr, t, ic.pointerOrNullPointer)
            this
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
    fun seekFrame(t: Float, ic: InvalidationController?): Animation =
        nativeCall(ic) { ic ->
            _nSeekFrame(nativePtr, t, ic.pointerOrNullPointer)
            this
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
    fun seekFrameTime(t: Float, ic: InvalidationController?): Animation =
        nativeCall(ic) { ic ->
            _nSeekFrameTime(nativePtr, t, ic.pointerOrNullPointer)
            this
        }

    /**
     * @return  the animation duration in seconds
     */
    val duration: Float
        get() = nativeCall { _nGetDuration(nativePtr) }

    /**
     * @return  the animation frame rate (frames / second)
     */
    val fPS: Float
        get() = nativeCall { _nGetFPS(nativePtr) }

    /**
     * @return  Animation in point, in frame index units
     */
    val inPoint: Float
        get() = nativeCall { _nGetInPoint(nativePtr) }

    /**
     * @return  Animation out point, in frame index units
     */
    val outPoint: Float
        get() = nativeCall { _nGetOutPoint(nativePtr) }
    val version: String
        get() = nativeCall { withStringReferenceResult { _nGetVersion(nativePtr) } }

    private var _size: Point? = null
    val size: Point
        get() {
            if (_size == null) {
                _size = nativeCall { Point.fromInteropPointer { _nGetSize(nativePtr, it) } }
            }
            return _size!!
        }
    val width: Float
        get() = size.x
    val height: Float
        get() = size.y
}

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetFinalizer")
// No ForceNativeThreadState: returns the function that frees the object, without calling it, so it is short and cannot
// block.
// Native call path: deleteAnimation
private external fun _nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nMakeFromString")
// Needs ForceNativeThreadState: parses the JSON string, so its time grows with its length, and may send log messages
// back into Kotlin.
// Native call path: Animation::Builder::make -> AnimationBuilder::parse
@ForceNativeThreadState
private external fun _nMakeFromString(data: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nMakeFromFile")
// Needs ForceNativeThreadState: reads the animation file from disk, which blocks on file I/O, and then parses it.
// Native call path: Animation::MakeFromFile -> Builder::makeFromFile -> SkData::MakeFromFileName
@ForceNativeThreadState
internal external fun _nMakeFromFile(path: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nMakeFromData")
// Needs ForceNativeThreadState: copies and parses the stream, so its time grows with the data size, and can invoke a
// logger callback.
// Native call path: Animation::Make -> SkData::MakeFromStream -> Builder::make
@ForceNativeThreadState
private external fun _nMakeFromData(dataPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nRender")
// Needs ForceNativeThreadState: draws the scene on a canvas that can be GPU-backed, including text and images.
// Native call path: Animation::render -> RenderNode::render
@ForceNativeThreadState
private external fun _nRender(
    ptr: NativePointer,
    canvasPtr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    flags: Int
)


@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nSeek")
// Needs ForceNativeThreadState: can use font APIs to lay out text again when moving to an animation frame, then updates
// the scene.
// Native call path: Animation::seekFrame -> TextAdapter::onSync -> TextAdapter::reshape -> Shaper::Shape
@ForceNativeThreadState
private external fun _nSeek(ptr: NativePointer, t: Float, icPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nSeekFrame")
// Needs ForceNativeThreadState: can use font APIs to lay out text again when moving to an animation frame, then updates
// the scene.
// Native call path: Animation::seekFrame -> TextAdapter::onSync -> TextAdapter::reshape -> Shaper::Shape
@ForceNativeThreadState
private external fun _nSeekFrame(ptr: NativePointer, t: Float, icPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nSeekFrameTime")
// Needs ForceNativeThreadState: can use font APIs to lay out text again when moving to an animation frame, then updates
// the scene.
// Native call path: Animation::seekFrame -> TextAdapter::onSync -> TextAdapter::reshape -> Shaper::Shape
@ForceNativeThreadState
private external fun _nSeekFrameTime(ptr: NativePointer, t: Float, icPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetDuration")
// No ForceNativeThreadState: returns a stored duration value and calls nothing, so it is short and cannot block.
// Native call path: Animation::duration
private external fun _nGetDuration(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetFPS")
// No ForceNativeThreadState: returns a stored frame rate value and calls nothing, so it is short and cannot block.
// Native call path: Animation::fps
private external fun _nGetFPS(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetInPoint")
// No ForceNativeThreadState: returns a stored in-point value and calls nothing, so it is short and cannot block.
// Native call path: Animation::inPoint
private external fun _nGetInPoint(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetOutPoint")
// No ForceNativeThreadState: returns a stored out-point value and calls nothing, so it is short and cannot block.
// Native call path: Animation::outPoint
private external fun _nGetOutPoint(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetVersion")
// No ForceNativeThreadState: returns a pointer to the stored version string without copying it, so it is short and
// cannot block.
// Native call path: Animation::version
private external fun _nGetVersion(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_skottie_Animation__1nGetSize")
// No ForceNativeThreadState: returns the stored size, which is written as two floats, so it is short and cannot block.
// Native call path: Animation::size -> Point::copyToInterop
private external fun _nGetSize(ptr: NativePointer, dst: InteropPointer)
// </editor-fold>