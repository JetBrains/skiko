package org.jetbrains.skia.impl

import java.lang.ref.Reference

actual abstract class RefCnt : Managed {
    protected actual constructor(ptr: NativePointer) : super(ptr, _FinalizerHolder.PTR, true)
    protected actual constructor(ptr: NativePointer, allowClose: Boolean) : super(ptr, _FinalizerHolder.PTR, allowClose)

    actual val refCount: Int
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

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    companion object {
        @JvmStatic
        external fun _nGetFinalizer(): NativePointer
        @JvmStatic
        external fun _nGetRefCount(ptr: NativePointer): Int
    }
}