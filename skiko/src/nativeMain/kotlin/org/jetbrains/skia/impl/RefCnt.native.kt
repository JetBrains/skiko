package org.jetbrains.skia.impl

import org.jetbrains.skia.ExternalSymbolName

actual abstract class RefCnt : Managed {
    protected actual constructor(ptr: NativePointer) : super(ptr, _FinalizerHolder.PTR, true) {}
    protected actual constructor(ptr: NativePointer, allowClose: Boolean) : super(ptr, _FinalizerHolder.PTR, allowClose)

    actual val refCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetRefCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    override fun toString(): String {
        val s = super.toString()
        return s.substring(0, s.length - 1) + ", refCount=" + refCount + ")"
    }

    private object _FinalizerHolder {
        val PTR = RefCnt_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_impl_RefCnt__getFinalizer")
internal actual external fun RefCnt_nGetFinalizer(): NativePointer
@ExternalSymbolName("org_jetbrains_skia_impl_RefCnt__getRefCount")
private external fun _nGetRefCount(ptr: NativePointer): Int
