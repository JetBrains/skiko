package org.jetbrains.skia.impl

import java.lang.ref.Cleaner

actual abstract class Managed actual constructor(ptr: Long, finalizer: Long, managed: Boolean)
    : Native(ptr), AutoCloseable {
    private var cleanable: Cleaner.Cleanable? = null
    actual override fun close() {
        if (0L == _ptr)
            throw RuntimeException("Object already closed: $javaClass, _ptr=$_ptr")
        else if (null == cleanable)
            throw RuntimeException("Object is not managed in JVM, can't close(): $javaClass, _ptr=$_ptr")
        else {
            cleanable!!.clean()
            cleanable = null
            _ptr = 0
        }
    }

    actual open val isClosed: Boolean
        get() = _ptr == 0L

    class CleanerThunk(var className: String, var ptr: Long, var finalizerPtr: Long) : Runnable {
        override fun run() {
            Log.trace { "Cleaning $className ${java.lang.Long.toString(ptr, 16)}" }
            Stats.onDeallocated(className)
            Stats.onNativeCall()
            _nInvokeFinalizer(finalizerPtr, ptr)
        }
    }

    companion object {
        var CLEANER = Cleaner.create()
        @JvmStatic external fun _nInvokeFinalizer(finalizer: Long, ptr: Long)
    }

    init {
        if (managed) {
            assert(ptr != 0L) { "Managed ptr is 0" }
            assert(finalizer != 0L) { "Managed finalizer is 0" }
            val className = javaClass.simpleName
            Stats.onAllocated(className)
            cleanable = CLEANER.register(this, CleanerThunk(className, ptr, finalizer))
        }
    }
}