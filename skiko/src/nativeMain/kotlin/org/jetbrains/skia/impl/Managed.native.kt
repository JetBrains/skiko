package org.jetbrains.skia.impl

import kotlinx.cinterop.nativeNullPtr
import org.jetbrains.skia.ExternalSymbolName
import kotlin.native.concurrent.AtomicNativePtr
import kotlin.native.concurrent.freeze
import kotlin.native.internal.createCleaner

private class FinalizationThunk(private val finalizer: NativePointer, val className: String, obj: NativePointer) {

    private var obj = AtomicNativePtr(obj)

    fun clean() {
        val ptr = obj.value
        if (ptr != nativeNullPtr && obj.compareAndSet(ptr, nativeNullPtr)) {
            Stats.onDeallocated(className)
            Stats.onNativeCall()
            _nInvokeFinalizer(finalizer, ptr)
        }
    }
    val isActive get() =
        obj.value != nativeNullPtr
}

actual abstract class Managed actual constructor(
        ptr: NativePointer, finalizer: NativePointer, managed: Boolean) : Native(ptr) {

    private val thunk: FinalizationThunk? = if (managed) {
        require(ptr != NullPointer) { "Managed ptr is nullptr" }
        require(finalizer != NullPointer) { "Managed finalizer is nullptr" }
        val className = this::class.simpleName ?: "<kotlin>"
        Stats.onAllocated(className)
        FinalizationThunk(finalizer, className, ptr).freeze()
    } else null

    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = if (managed) {
        createCleaner(thunk) {
            it?.clean()
        }
    } else null

    actual open fun close() {
        require(_ptr != NullPointer) {
            "Object already closed: ${this::class.simpleName}, _ptr=$_ptr"
        }
        requireNotNull(thunk) {
            "Object is not managed in K/N runtime, can't close(): ${this::class.simpleName}, _ptr=$_ptr"
        }
        require(thunk.isActive) {
            "Object is closed already, can't close(): ${this::class.simpleName}, _ptr=$_ptr"
        }

        thunk.clean()
        _ptr = NullPointer
    }

    actual open val isClosed: Boolean
        get() = _ptr == NullPointer
}

@ExternalSymbolName("org_jetbrains_skia_impl_Managed__invokeFinalizer")
external fun _nInvokeFinalizer(finalizer: NativePointer, ptr: NativePointer)
