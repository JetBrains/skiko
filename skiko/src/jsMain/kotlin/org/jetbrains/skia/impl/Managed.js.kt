package org.jetbrains.skia.impl

private class FinalizationThunk(private val finalizer: NativePointer, private var obj: NativePointer) {
    fun clean() {
        if (obj != 0)
            _nInvokeFinalizer(finalizer, obj)
        obj = 0
    }
}

internal external class FinalizationRegistry(cleanup: (dynamic) -> Unit) {
    fun register(obj: dynamic, handle: dynamic)
    fun unregister(obj: dynamic)
}

private val registry = FinalizationRegistry {
    val thunk = it as FinalizationThunk
    thunk.clean()
}

private fun register(managed: Managed, thunk: FinalizationThunk) {
    registry.register(managed, thunk)
}

private fun unregister(managed: Managed) {
    registry.unregister(managed)
}

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

@JsName("org_jetbrains_skia_impl_Managed__invokeFinalizer")
private external fun _nInvokeFinalizer(finalizer: NativePointer, obj: NativePointer)