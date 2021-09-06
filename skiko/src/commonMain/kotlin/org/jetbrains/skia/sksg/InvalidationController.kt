@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.sksg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

/**
 *
 * Receiver for invalidation events.
 *
 * Tracks dirty regions for repaint.
 */
class InvalidationController internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        external fun _nGetFinalizer(): Long
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
            matrix?.mat ?: Matrix33.IDENTITY.mat
        )
        return this
    }

    val bounds: Rect
        get() = try {
            Stats.onNativeCall()
            _nGetBounds(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun reset(): InvalidationController {
        Stats.onNativeCall()
        _nReset(_ptr)
        return this
    }
}