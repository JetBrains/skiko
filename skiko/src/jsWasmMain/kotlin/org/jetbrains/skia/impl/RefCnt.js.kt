package org.jetbrains.skia.impl

import org.jetbrains.skia.ExternalCode
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.InteropType

actual abstract class RefCnt : Managed {
    actual protected constructor(ptr: NativePointer): super(ptr, _FinalizerHolder.PTR, true)
    actual protected constructor(ptr: NativePointer, allowClose: Boolean): super(ptr, _FinalizerHolder.PTR, allowClose)

    actual val refCount: Int
        get() {
            Stats.onNativeCall()
            return _nGetRefCount(_ptr)
        }

    override fun toString(): String {
        val s = super.toString()
        return s.substring(0, s.length - 1) + ", refCount=" + refCount + ")"
    }
}

private object _FinalizerHolder {
    val PTR = RefCnt_nGetFinalizer()
}

@ExternalSymbolName("org_jetbrains_skia_impl_RefCnt__getFinalizer")
@ExternalCode("Module['asm']['org_jetbrains_skia_impl_RefCnt__getFinalizer']")
internal actual external fun RefCnt_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_impl_RefCnt__getRefCount")
@ExternalCode("Module['asm']['org_jetbrains_skia_impl_RefCnt__getRefCount']")
private external fun _nGetRefCount(ptr: NativePointer): Int
