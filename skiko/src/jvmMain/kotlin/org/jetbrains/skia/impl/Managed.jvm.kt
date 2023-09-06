package org.jetbrains.skia.impl

import org.jetbrains.skiko.Logger
import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import kotlin.concurrent.thread

// Android doesn't have Cleaner API, so use explicit phantom references + finalization queue.
// Consider using this on all JVM platforms eventually.
@OptIn(kotlin.ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
actual abstract class Managed actual constructor(
    ptr: Long, finalizer: Long, managed: Boolean
) : Native(ptr), AutoCloseable {

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
            Logger.trace { "Cleaning $className ${ptr.toString(16)}" }
            Stats.onDeallocated(className)
            Stats.onNativeCall()
            _nInvokeFinalizer(finalizerPtr, ptr)
        }
    }

    private var cleanable: Cleanable? = null

    companion object {
        private val CLEANER = Cleaner()

        @JvmStatic
        external fun _nInvokeFinalizer(finalizer: Long, ptr: Long)
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

private interface Cleanable {
    fun clean()

    var prev: Cleanable?
    var next: Cleanable?
}

private class CleanableImpl(managed: Managed, action: Runnable, cleaner: Cleaner) :
    PhantomReference<Managed>(managed, cleaner.queue), Cleanable {

    override var prev: Cleanable? = this
    override var next: Cleanable? = this

    private val list: Cleanable = cleaner.list
    private var action: Runnable = action

    init {
        insert()
        reachabilityFence(managed)
        reachabilityFence(cleaner)
    }

    override fun clean() {
        if (remove()) {
            super.clear()
            action.run()
        }
    }

    override fun clear() {
        throw UnsupportedOperationException("clear() unsupported")
    }

    private fun insert() {
        synchronized(list) {
            prev = list
            next = list.next
            next?.prev = this
            list.next = this
        }
    }

    private fun remove(): Boolean {
        synchronized(list) {
            if (next !== this) {
                next?.prev = prev
                prev?.next = next
                prev = this
                next = this
                return true
            }
            return false
        }
    }
}

private class Cleaner {
    val queue = ReferenceQueue<Managed>()
    var list: Cleanable = object : Cleanable {
        override fun clean() {
            TODO("Must not be called")
        }

        override var prev: Cleanable? = null
        override var next: Cleanable? = null
    }

    @Volatile
    var stopped = false

    init {
        thread(start = true, isDaemon = true, name = "Reference Cleaner") {
            while (!stopped) {
                val ref = queue.remove(60 * 1000L) as Cleanable?
                try {
                    ref?.clean()
                } catch (t: Throwable) {
                }
            }
        }
    }

    fun register(managed: Managed, action: Runnable): Cleanable {
        return CleanableImpl(managed, action, this)
    }

    fun stop() {
        stopped = true
    }
}
