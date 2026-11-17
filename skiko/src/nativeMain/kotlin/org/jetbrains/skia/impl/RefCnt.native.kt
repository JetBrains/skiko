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

    override fun toString() = refCntToString(super.toString(), NullPointer)

    private object _FinalizerHolder {
        val PTR = RefCnt_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_impl_RefCnt__getFinalizer")
// No ForceNativeThreadState: only returns the address of an unref function, so it is short and cannot block.
// Native call path: unrefSkRefCnt
internal actual external fun RefCnt_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_impl_RefCnt__getRefCount")
// No ForceNativeThreadState: does one relaxed atomic read of a counter, so it is short and cannot block.
// Native call path: std::atomic<int32_t>::load
private external fun _nGetRefCount(ptr: NativePointer): Int
