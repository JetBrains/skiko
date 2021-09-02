package org.jetbrains.skia.impl

import java.lang.ref.Cleaner

actual abstract class Managed actual constructor(ptr: Long, finalizer: Long, managed: Boolean) : Native(ptr),
    AutoCloseable {
    private var _cleanable: Cleaner.Cleanable? = null
    override fun close() {
        if (0L == _ptr) throw RuntimeException("Object already closed: $javaClass, _ptr=$_ptr") else if (null == _cleanable) throw RuntimeException(
            "Object is not managed in JVM, can't close(): $javaClass, _ptr=$_ptr"
        ) else {
            _cleanable!!.clean()
            _cleanable = null
            _ptr = 0
        }
    }

    open val isClosed: Boolean
        get() = _ptr == 0L

    class CleanerThunk(var _className: String, var _ptr: Long, var _finalizerPtr: Long) : Runnable {
        override fun run() {
            Log.trace { "Cleaning " + _className + " " + java.lang.Long.toString(_ptr, 16) }
            Stats.onDeallocated(_className)
            Stats.onNativeCall()
            _nInvokeFinalizer(_finalizerPtr, _ptr)
        }
    }

    companion object {
        var _cleaner = Cleaner.create()
        @JvmStatic external fun _nInvokeFinalizer(finalizer: Long, ptr: Long)
    }

    init {
        if (managed) {
            assert(ptr != 0L) { "Managed ptr is 0" }
            assert(finalizer != 0L) { "Managed finalizer is 0" }
            val className = javaClass.simpleName
            Stats.onAllocated(className)
            _cleanable = _cleaner.register(this, CleanerThunk(className, ptr, finalizer))
        }
    }
}