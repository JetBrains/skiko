package org.jetbrains.skiko

import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.posix.*
import kotlin.coroutines.CoroutineContext

object SkikoDispatchers {
    val Main: CoroutineDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            LinuxMainDispatcher.dispatch(block)
        }
    }
}

object LinuxMainDispatcher {
    private val queue = ArrayDeque<Runnable>()
    private val mutex = nativeHeap.alloc<pthread_mutex_t>()

    init {
        pthread_mutex_init(mutex.ptr, null)
    }

    private inline fun <T> withLock(block: () -> T): T {
        pthread_mutex_lock(mutex.ptr)
        return try {
            block()
        } finally {
            pthread_mutex_unlock(mutex.ptr)
        }
    }

    fun dispatch(block: Runnable) {
        withLock { queue.addLast(block) }
    }

    fun drain() {
        withLock { queue.toList().also { queue.clear() } }
            .forEach { runCatching(it::run).onFailure(Throwable::printStackTrace) }
    }
}
