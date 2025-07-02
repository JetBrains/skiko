package org.jetbrains.skia.sksg

import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

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