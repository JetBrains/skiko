package org.jetbrains.skija.sksg

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.*
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

/**
 *
 * Receiver for invalidation events.
 *
 * Tracks dirty regions for repaint.
 */
class InvalidationController internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMake(): Long
        @JvmStatic external fun _nInvalidate(ptr: Long, left: Float, top: Float, right: Float, bottom: Float, matrix: FloatArray?)
        @JvmStatic external fun _nGetBounds(ptr: Long): Rect
        @JvmStatic external fun _nReset(ptr: Long)

        init {
            staticLoad()
        }
    }

    internal object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    constructor() : this(_nMake()) {}

    fun invalidate(left: Float, top: Float, right: Float, bottom: Float, matrix: Matrix33?): InvalidationController {
        Stats.onNativeCall()
        _nInvalidate(
            _ptr,
            left,
            top,
            right,
            bottom,
            if (matrix == null) Matrix33.Companion.IDENTITY.mat else matrix.mat
        )
        return this
    }

    val bounds: Rect
        get() = try {
            Stats.onNativeCall()
            _nGetBounds(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun reset(): InvalidationController {
        Stats.onNativeCall()
        _nReset(_ptr)
        return this
    }
}