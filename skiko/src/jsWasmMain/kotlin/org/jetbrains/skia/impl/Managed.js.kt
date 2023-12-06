package org.jetbrains.skia.impl

import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.ExternalSymbolName

internal class FinalizationThunk(private val finalizer: NativePointer, private var obj: NativePointer) {
    fun clean() {
        if (obj != 0)
            _nInvokeFinalizer(finalizer, obj)
        obj = 0
    }
}

internal expect fun register(managed: Managed, thunk: FinalizationThunk)

internal expect fun unregister(managed: Managed)

actual abstract class Managed actual constructor(ptr: NativePointer, finalizer: NativePointer, managed: Boolean) : Native(ptr) {
    private var cleaner: FinalizationThunk? = null

    actual open fun close() {
        if (NullPointer == _ptr)
            throw RuntimeException("Object already closed: ${this::class.simpleName}, _ptr=$_ptr")
        else if (null == cleaner)
            throw RuntimeException("Object is not managed, can't close(): ${this::class.simpleName}, _ptr=$_ptr")
        else {
            unregister(this)
            cleaner!!.clean()
            cleaner = null
            _ptr = 0
        }
    }

    actual open val isClosed: Boolean
        get() = _ptr == NullPointer

    init {
        if (managed) {
            require(ptr != 0) { "Managed ptr is 0" }
            require(finalizer != 0) { "Managed finalizer is 0" }
            val thunk = FinalizationThunk(finalizer, ptr)
            register(this, thunk)
            cleaner = thunk
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_impl_Managed__invokeFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_impl_Managed__invokeFinalizer")
private external fun _nInvokeFinalizer(finalizer: NativePointer, obj: NativePointer)