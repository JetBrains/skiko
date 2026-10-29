package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class PictureRecorder internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(PictureRecorder_nMake()) {
        Stats.onNativeCall()
    }

    private object _FinalizerHolder {
        val PTR = PictureRecorder_nGetFinalizer()
    }

    /**
     * Returns the canvas that records the drawing commands.
     *
     * @param bounds the cull rect used when recording this picture. Any drawing the falls outside
     * of this rect is undefined, and may be drawn or it may not.
     * @param bbh optional acceleration structure
     * @return the canvas.
     */
    fun beginRecording(bounds: Rect, bbh: BBHFactory? = null): Canvas = beginRecording(
        bounds.left,
        bounds.top,
        bounds.right,
        bounds.bottom,
        bbh
    )

    /**
     * Returns the canvas that records the drawing commands.
     *
     * @param left   the left side of the cull rect used when recording this picture. Any drawing
     *               that falls outside of this rect is undefined and may be drawn, or it may not.
     * @param top    the top side of the cull rect used when recording this picture. Any drawing
     *               that falls outside of this rect is undefined and may be drawn, or it may not.
     * @param right  the right side of the cull rect used when recording this picture. Any drawing
     *               that falls outside of this rect is undefined and may be drawn, or it may not.
     * @param bottom the bottom side of the cull rect used when recording this picture. Any drawing
     *               that falls outside of this rect is undefined and may be drawn, or it may not.
     * @param bbh    optional acceleration structure
     * @return the canvas.
     */
    fun beginRecording(left: Float, top: Float, right: Float, bottom: Float, bbh: BBHFactory? = null): Canvas =
        nativeCall(bbh) { bbh ->
            Canvas(
                _nBeginRecording(
                    _ptr,
                    left,
                    top,
                    right,
                    bottom,
                    bbh.pointerOrNullPointer
                ), false, this
            )
        }

    /**
     * @return  the recording canvas if one is active, or null if recording is not active.
     */
    val recordingCanvas: Canvas?
        get() = nativeCall {
            val ptr = _nGetRecordingCanvas(_ptr)
            if (ptr == NullPointer) null else Canvas(ptr, false, this)
        }

    /**
     *
     * Signal that the caller is done recording. This invalidates the canvas returned by
     * [.beginRecording]/[.getRecordingCanvas].
     *
     *
     * The returned picture is immutable. If during recording drawables were added to the canvas,
     * these will have been "drawn" into a recording canvas, so that this resulting picture will
     * reflect their current state, but will not contain a live reference to the drawables
     * themselves.
     */
    fun finishRecordingAsPicture(): Picture = nativeCall { Picture(_nFinishRecordingAsPicture(_ptr)) }

    /**
     * Signal that the caller is done recording, and update the cull rect to use for bounding
     * box hierarchy (BBH) generation. The behavior is the same as calling
     * [.finishRecordingAsPicture], except that this method updates the cull rect
     * initially passed into [.beginRecording].
     *
     * @param cull the new culling rectangle to use as the overall bound for BBH generation
     * and subsequent culling operations.
     * @return the picture containing the recorded content.
     */
    fun finishRecordingAsPicture(cull: Rect): Picture = finishRecordingAsPicture(
        cull.left,
        cull.top,
        cull.right,
        cull.bottom
    )

    /**
     * Finalizes the recording of the drawing commands and creates an immutable picture object
     * that encapsulates the recorded content. The cull rect provided defines the boundaries
     * for the recorded content and can be used for bounding box hierarchy (BBH) generation
     * and subsequent culling operations. After this call, the canvas returned by any
     * `beginRecording` or `getRecordingCanvas` method becomes invalid.
     *
     * @param cullLeft The left side of the cull rect defining the visible bounds of the recording.
     *                 Any drawing outside this boundary may or may not be included in the result.
     * @param cullTop The top side of the cull rect defining the visible bounds of the recording.
     *                Any drawing outside this boundary may or may not be included in the result.
     * @param cullRight The right side of the cull rect defining the visible bounds of the recording.
     *                  Any drawing outside this boundary may or may not be included in the result.
     * @param cullBottom The bottom side of the cull rect defining the visible bounds of the recording.
     *                   Any drawing outside this boundary may or may not be included in the result.
     * @return An immutable [Picture] object that contains the recorded drawing commands.
     */
    fun finishRecordingAsPicture(cullLeft: Float, cullTop: Float, cullRight: Float, cullBottom: Float): Picture = nativeCall {
        Picture(
            _nFinishRecordingAsPictureWithCull(
                _ptr,
                cullLeft,
                cullTop,
                cullRight,
                cullBottom
            )
        )
    }
//TODO
//    /**
//     *
//     * Signal that the caller is done recording. This invalidates the canvas returned by
//     * [.beginRecording]/[.getRecordingCanvas].
//     *
//     *
//     * Unlike [.finishRecordingAsPicture], which returns an immutable picture,
//     * the returned drawable may contain live references to other drawables (if they were added to
//     * the recording canvas) and therefore this drawable will reflect the current state of those
//     * nested drawables anytime it is drawn or a new picture is snapped from it (by calling
//     * [Drawable.makePictureSnapshot]).
//     */
//     fun finishRecordingAsDrawable(cullLeft: Float, cullTop: Float, cullRight: Float, cullBottom: Float) : Drawable = nativeCall {
//         Drawable(
//             _nFinishRecordingAsDrawable(
//                 _ptr,
//                 cullLeft,
//                 cullTop,
//                 cullRight,
//                 cullBottom
//             )
//         )
//     }
}


// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nMake")
// No ForceNativeThreadState: creates a new empty recorder that has nothing recorded yet, so it is short and cannot
// block.
// Native call path: SkPictureRecorder::SkPictureRecorder
private external fun PictureRecorder_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of the deletion function, so it is short and cannot block.
// Native call path: deletePictureRecorder
private external fun PictureRecorder_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nBeginRecording")
// Needs ForceNativeThreadState: starting a new recording drops the drawables of the last one, and the last reference
// can free a picture or typeface.
// Native call path: SkPictureRecorder::beginRecording -> SkRecordCanvas::forgetRecord -> ~SkDrawableList
@ForceNativeThreadState
private external fun _nBeginRecording(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    bbh: NativePointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nGetRecordingCanvas")
// No ForceNativeThreadState: returns the recorder's stored canvas, or null when it is not recording, so it is short and
// cannot block.
// Native call path: SkPictureRecorder::getRecordingCanvas
private external fun _nGetRecordingCanvas(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPicture")
// Needs ForceNativeThreadState: optimizes all recorded drawing operations and snapshots drawable objects through Kotlin
// onDraw callbacks; work grows with the operation count.
// Native call path: finishRecordingAsPicture -> newDrawableSnapshot -> SkDrawable::onDraw
@ForceNativeThreadState
private external fun _nFinishRecordingAsPicture(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsPictureWithCull")
// Needs ForceNativeThreadState: optimizes all recorded drawing operations and snapshots drawable objects through Kotlin
// onDraw callbacks; work grows with the operation count.
// Native call path: finishRecordingAsPicture -> newDrawableSnapshot -> SkDrawable::onDraw
@ForceNativeThreadState
private external fun _nFinishRecordingAsPictureWithCull(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_PictureRecorder__1nFinishRecordingAsDrawable")
// Needs ForceNativeThreadState: optimizes the recorded drawing operations and computes their bounds, so its time grows
// with the number of operations.
// Native call path: finishRecordingAsDrawable -> SkRecordOptimize
@ForceNativeThreadState
private external fun _nFinishRecordingAsDrawable(ptr: NativePointer): NativePointer
// </editor-fold>
