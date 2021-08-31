package org.jetbrains.skija.sksg

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Contract
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
class InvalidationController @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        external fun _nGetFinalizer(): Long
        external fun _nMake(): Long
        external fun _nInvalidate(ptr: Long, left: Float, top: Float, right: Float, bottom: Float, matrix: FloatArray?)
        external fun _nGetBounds(ptr: Long): Rect
        external fun _nReset(ptr: Long)

        init {
            staticLoad()
        }
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    constructor() : this(_nMake()) {}

    @Contract("-> this")
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

    @Contract("-> this")
    fun reset(): InvalidationController {
        Stats.onNativeCall()
        _nReset(_ptr)
        return this
    }
}