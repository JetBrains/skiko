package org.jetbrains.skija.impl

import org.jetbrains.annotations.ApiStatus
import java.lang.ref.Reference

abstract class RefCnt : Managed {
    protected constructor(ptr: Long) : super(ptr, _FinalizerHolder.PTR) {}
    protected constructor(ptr: Long, allowClose: Boolean) : super(ptr, _FinalizerHolder.PTR, allowClose) {}

    val refCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetRefCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    override fun toString(): String {
        val s = super.toString()
        return s.substring(0, s.length - 1) + ", refCount=" + refCount + ")"
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    companion object {
        @JvmStatic
        external fun _nGetFinalizer(): Long
        @JvmStatic
        external fun _nGetRefCount(ptr: Long): Int
    }
}