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
    fun draw(canvas: Canvas?, matrix: Matrix33?): Drawable {
        return try {
            Stats.onNativeCall()
            interopScope {
                _nDraw(
                    _ptr,
                    getPtr(canvas),
                    toInterop(matrix?.mat)
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(canvas)
        }
    }

    fun makePictureSnapshot(): Picture {
        return try {
            Stats.onNativeCall()
            Picture(_nMakePictureSnapshot(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

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
        get() = try {
            Stats.onNativeCall()
            Drawable_nGetGenerationId(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nGetFinalizer")
private external fun Drawable_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nMake")
private external fun Drawable_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetGenerationId")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nGetGenerationId")
private external fun Drawable_nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nDraw")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nDraw")
private external fun _nDraw(ptr: NativePointer, canvasPtr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nMakePictureSnapshot")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nMakePictureSnapshot")
private external fun _nMakePictureSnapshot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nNotifyDrawingChanged")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nNotifyDrawingChanged")
private external fun _nNotifyDrawingChanged(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetBounds")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nGetBounds")
private external fun Drawable_nGetBounds(ptr: NativePointer, result: InteropPointer)

// For Native/JS usage only

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nInit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nInit")
internal external fun Drawable_nInit(ptr: NativePointer, onGetBounds: InteropPointer, onDraw: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetOnDrawCanvas")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nGetOnDrawCanvas")
internal external fun _nGetOnDrawCanvas(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nSetBounds")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Drawable__1nSetBounds")
internal external fun _nSetBounds(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float)