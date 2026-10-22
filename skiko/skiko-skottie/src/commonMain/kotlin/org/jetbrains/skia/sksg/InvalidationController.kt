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

    fun invalidate(left: Float, top: Float, right: Float, bottom: Float, matrix: Matrix33?): InvalidationController {
        Stats.onNativeCall()
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
        return this
    }

    val bounds: Rect
        get() = try {
            Stats.onNativeCall()
            Rect.fromInteropPointer { InvalidationController_nGetBounds(nativePtr, it) }
        } finally {
            reachabilityBarrier(this)
        }

    fun reset(): InvalidationController {
        Stats.onNativeCall()

        InvalidationController_nReset(nativePtr)
        return this
    }
}

// Runnable: returns the address of a delete function; the finalizer runs through Managed__invokeFinalizer
@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nGetFinalizer")
private external fun InvalidationController_nGetFinalizer(): NativePointer

// Runnable: new InvalidationController holds an empty std::vector<SkRect> and an empty SkRect
@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nMake")
private external fun InvalidationController_nMake(): NativePointer

// Native: InvalidationController::inval -> std::vector::push_back can relocate every stored SkRect
@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nInvalidate")
@ForceNativeThreadState
private external fun InvalidationController_nInvalidate(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, matrix: InteropPointer)

// Runnable: InvalidationController::bounds returns stored fBounds; copyToInterop writes four floats
@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nGetBounds")
private external fun InvalidationController_nGetBounds(ptr: NativePointer, result: InteropPointer)

// Native: InvalidationController::reset -> std::vector::clear can walk every stored SkRect
@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nReset")
@ForceNativeThreadState
private external fun InvalidationController_nReset(ptr: NativePointer)
