package org.jetbrains.skia.sksg

import org.jetbrains.skia.skottie.SkottieLibrary
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*

/**
 *
 * Receiver for invalidation events.
 *
 * Tracks dirty regions for repaint.
 */
class InvalidationController internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            SkottieLibrary.load()
        }
    }

    internal object _FinalizerHolder {
        val PTR = InvalidationController_nGetFinalizer()
    }

    constructor() : this(InvalidationController_nMake()) {}

    fun invalidate(left: Float, top: Float, right: Float, bottom: Float, matrix: Matrix33?): InvalidationController =
        nativeCall {
            interopScope {
                val mat = matrix?.mat ?: Matrix33.IDENTITY.mat
                InvalidationController_nInvalidate(
                    nativePtr,
                    left,
                    top,
                    right,
                    bottom,
                    toInterop(mat)
                )
            }
            this
        }

    val bounds: Rect
        get() = nativeCall { Rect.fromInteropPointer { InvalidationController_nGetBounds(nativePtr, it) } }

    fun reset(): InvalidationController = nativeCall {
        InvalidationController_nReset(nativePtr)
        this
    }

}

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nGetFinalizer")
// No ForceNativeThreadState: returns the function that frees the object, without calling it, so it is short and cannot
// block.
// Native call path: deleteInvalidationController
private external fun InvalidationController_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nMake")
// No ForceNativeThreadState: creates an empty controller with an empty rectangle and no stored rectangles, so it is
// short and cannot block.
// Native call path: InvalidationController::InvalidationController
private external fun InvalidationController_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nInvalidate")
// Needs ForceNativeThreadState: adding a rectangle can grow the list and move every stored rectangle, so its time grows
// with the list size.
// Native call path: InvalidationController::inval -> std::vector::push_back
@ForceNativeThreadState
private external fun InvalidationController_nInvalidate(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nGetBounds")
// No ForceNativeThreadState: returns the stored bounds, which are written as four floats, so it is short and cannot
// block.
// Native call path: InvalidationController::bounds -> Rect::copyToInterop
private external fun InvalidationController_nGetBounds(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nReset")
// Needs ForceNativeThreadState: clearing the list walks every stored rectangle, so its time grows with the list size.
// Native call path: InvalidationController::reset -> std::vector::clear
@ForceNativeThreadState
private external fun InvalidationController_nReset(ptr: NativePointer)
