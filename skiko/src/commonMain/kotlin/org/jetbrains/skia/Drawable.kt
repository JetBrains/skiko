package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skiko.maybeSynchronized

/**
 *
 * Base class for objects that draw into Canvas.
 *
 *
 * The object has a generation id, which is guaranteed to be unique across all drawables. To
 * allow for clients of the drawable that may want to cache the results, the drawable must
 * change its generation id whenever its internal state changes such that it will draw differently.
 */
abstract class Drawable : Managed(Drawable_nMake(), _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    private var _bounds: Rect? = null
    private val boundsLock = Unit

    /**
     * Draws into the specified content. The drawing sequence will be balanced upon return
     * (i.e. the saveLevel() on the canvas will match what it was when draw() was called,
     * and the current matrix and clip settings will not be changed.
     */
    fun draw(canvas: Canvas?): Drawable {
        return draw(canvas, null)
    }

    /**
     * Draws into the specified content. The drawing sequence will be balanced upon return
     * (i.e. the saveLevel() on the canvas will match what it was when draw() was called,
     * and the current matrix and clip settings will not be changed.
     */
    fun draw(canvas: Canvas?, x: Float, y: Float): Drawable {
        return draw(canvas, Matrix33.makeTranslate(x, y))
    }

    /**
     * Draws into the specified content. The drawing sequence will be balanced upon return
     * (i.e. the saveLevel() on the canvas will match what it was when draw() was called,
     * and the current matrix and clip settings will not be changed.
     */
    fun draw(canvas: Canvas?, matrix: Matrix33?): Drawable = nativeCall(canvas) { canvas ->
        interopScope {
            _nDraw(
                _ptr,
                canvas.pointerOrNullPointer,
                toInterop(matrix?.mat)
            )
        }
        this
    }

    fun makePictureSnapshot(): Picture = nativeCall { Picture(_nMakePictureSnapshot(_ptr)) }

    /**
     *
     * Return a unique value for this instance. If two calls to this return the same value,
     * it is presumed that calling the draw() method will render the same thing as well.
     *
     *
     * Subclasses that change their state should call notifyDrawingChanged() to ensure that
     * a new value will be returned the next time it is called.
     */
    val generationId: Int
        get() = nativeCall { Drawable_nGetGenerationId(_ptr) }

    /**
     * Return the (conservative) bounds of what the drawable will draw. If the drawable can
     * change what it draws (e.g. animation or in response to some external change), then this
     * must return a bounds that is always valid for all possible states.
     */
    val bounds: Rect
        get() {
            maybeSynchronized(boundsLock) {
                if (_bounds == null) _bounds = Rect.fromInteropPointer { Drawable_nGetBounds(_ptr, it) }
                return _bounds!!
            }
        }

    /**
     * Calling this invalidates the previous generation ID, and causes a new one to be computed
     * the next time getGenerationId() is called. Typically this is called by the object itself,
     * in response to its internal state changing.
     */
    fun notifyDrawingChanged(): Drawable {
        maybeSynchronized(boundsLock) {
            Stats.onNativeCall()
            _nNotifyDrawingChanged(_ptr)
            _bounds = null
        }
        return this
    }

    abstract fun onDraw(canvas: Canvas?)
    abstract fun onGetBounds(): Rect

    private fun _onDraw(canvasPtr: NativePointer) {
        onDraw(Canvas(canvasPtr, false, this))
    }

    private object _FinalizerHolder {
        val PTR = Drawable_nGetFinalizer()
    }

    init {
        doInit(_ptr)
    }
}

internal expect fun Drawable.doInit(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of a delete function without calling it, so it is short and cannot
// block.
// Native call path: _nGetFinalizer
private external fun Drawable_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nMake")
// No ForceNativeThreadState: creates a drawable with null callbacks and zeroes its generation id, so it is short and
// cannot block.
// Native call path: SkikoDrawable::SkikoDrawable -> SkDrawable::SkDrawable
private external fun Drawable_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetGenerationId")
// No ForceNativeThreadState: takes the next value from a shared atomic counter, so it is short and cannot block.
// Native call path: SkDrawable::getGenerationID -> next_generation_id
private external fun Drawable_nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nDraw")
// Needs ForceNativeThreadState: calls back into Kotlin to draw, and the canvas can use the GPU.
// Native call path: SkDrawable::draw -> SkikoDrawable::onDraw -> callVoidCallback
@ForceNativeThreadState
private external fun _nDraw(ptr: NativePointer, canvasPtr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nMakePictureSnapshot")
// Needs ForceNativeThreadState: calls back into Kotlin for bounds and drawing while recording a picture snapshot.
// Native call path: SkDrawable::onMakePictureSnapshot -> getBounds -> onGetBounds
@ForceNativeThreadState
private external fun _nMakePictureSnapshot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nNotifyDrawingChanged")
// No ForceNativeThreadState: resets a stored generation id field to zero, so it is short and cannot block.
// Native call path: SkDrawable::notifyDrawingChanged
private external fun _nNotifyDrawingChanged(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetBounds")
// Needs ForceNativeThreadState: calls back into Kotlin to read the drawable's bounds.
// Native call path: SkDrawable::getBounds -> SkikoDrawable::onGetBounds -> callVoidCallback
@ForceNativeThreadState
private external fun Drawable_nGetBounds(ptr: NativePointer, result: InteropPointer)

// For Native/JS usage only

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nInit")
// Needs ForceNativeThreadState: replacing a callback destroys the old callback and calls into Kotlin to dispose of it.
// Native call path: KCallback::operator= -> ~KCallback -> disposeCallback
@ForceNativeThreadState
internal external fun Drawable_nInit(ptr: NativePointer, onGetBounds: InteropPointer, onDraw: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetOnDrawCanvas")
// No ForceNativeThreadState: returns the stored pointer to the canvas used during the drawing callback, so it is short
// and cannot block.
// Native call path: SkikoDrawable::onDrawCanvas
internal external fun _nGetOnDrawCanvas(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nSetBounds")
// No ForceNativeThreadState: sets the four edges of the stored bounds rectangle, so it is short and cannot block.
// Native call path: SkRect::setLTRB
internal external fun _nSetBounds(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float)