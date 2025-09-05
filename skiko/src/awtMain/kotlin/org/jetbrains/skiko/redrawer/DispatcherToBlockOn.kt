package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

private val defaultFactory = Executors.defaultThreadFactory()

/**
 * Dispatcher intended for use in coroutines that blocks (not suspends) for indefinite amount of time.
 * Now we use it e.g. for waiting for VSYNC.
 * We can't use `Dispatchers.IO` here because it's limited by 64 threads and under heavy IO workload all of them might be occupied
 * which leads to skipped frames
 */
internal val dispatcherToBlockOn = Executors.newCachedThreadPool {
    defaultFactory.newThread(it).apply {
        isDaemon = true
        name = "skiko-dispatcher-to-block-on"
    }
}.asCoroutineDispatcher()