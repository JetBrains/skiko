package org.jetbrains.skia.impl

actual abstract class RefCnt : Managed {
    actual protected constructor(ptr: NativePointer): super(ptr, _FinalizerHolder.PTR, true)
    actual protected constructor(ptr: NativePointer, allowClose: Boolean): super(ptr, _FinalizerHolder.PTR, allowClose)

    actual val refCount: Int
        get() {
            Stats.onNativeCall()
            return RefCnt_nGetRefCount(_ptr)
        }

    override fun toString(): String {
        val s = super.toString()
        return s.substring(0, s.length - 1) + ", refCount=" + refCount + ")"
    }
}

private object _FinalizerHolder {
    val PTR = RefCnt_nGetFinalizer()
}