package org.jetbrains.skia.sksg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import kotlin.jvm.JvmStatic

/**
 *
 * Receiver for invalidation events.
 *
 * Tracks dirty regions for repaint.
 */
class InvalidationController internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
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
                _ptr,
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
            Rect.fromInteropPointer { InvalidationController_nGetBounds(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }

    fun reset(): InvalidationController {
        Stats.onNativeCall()

        InvalidationController_nReset(_ptr)
        return this
    }
}

@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nGetFinalizer")
@ExternalCode("Module['asm']['org_jetbrains_skia_sksg_InvalidationController_nGetFinalizer']")
private external fun InvalidationController_nGetFinalizer(): NativePointer
@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nMake")
@ExternalCode("Module['asm']['org_jetbrains_skia_sksg_InvalidationController_nMake']")
private external fun InvalidationController_nMake(): NativePointer
@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nInvalidate")
@ExternalCode("Module['asm']['org_jetbrains_skia_sksg_InvalidationController_nInvalidate']")
private external fun InvalidationController_nInvalidate(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, matrix: InteropPointer)
@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nGetBounds")
@ExternalCode("Module['asm']['org_jetbrains_skia_sksg_InvalidationController_nGetBounds']")
private external fun InvalidationController_nGetBounds(ptr: NativePointer, result: InteropPointer)
@ExternalSymbolName("org_jetbrains_skia_sksg_InvalidationController_nReset")
@ExternalCode("Module['asm']['org_jetbrains_skia_sksg_InvalidationController_nReset']")
private external fun InvalidationController_nReset(ptr: NativePointer)
