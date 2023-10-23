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

actual abstract class Managed actual constructor(ptr: NativePointer, finalizer: NativePointer, managed: Boolean) : Native() {
    private var cleaner: FinalizationThunk? = null

    private var __ptr: NativePointer = ptr.also {
        require(it != NullPointer) {
            "Can't wrap NullPointer"
        }
    }

    actual override val _ptr: NativePointer get() = __ptr.also {
        check(__ptr != NullPointer) {
            "Object already closed: ${this::class.simpleName}"
        }
    }

    actual open fun close() {
        check(__ptr != NullPointer) {
            "Object already closed: ${this::class.simpleName}"
        }
        check(null != cleaner) {
            "Object is not managed, can't close(): ${this::class.simpleName}, _ptr=$__ptr"
        }
        unregister(this)
        cleaner!!.clean()
        cleaner = null
        __ptr = 0
    }

    actual open val isClosed: Boolean
        get() = __ptr == NullPointer

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