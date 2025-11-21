package org.jetbrains.skiko

import java.nio.channels.FileChannel
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.io.path.createParentDirectories
import kotlin.use

/**
 * Capturing a file lock can only be done by a single process:
 * In order to express locking, not only across processes, but also within the same process (thread),
 * we introduce locks which are associated for locked file paths.
 */
private val fileLockMutex = AtomicReference<Map<Path, ReentrantLock>>(emptyMap())

/**
 * Simple lockfile utility which ensures that the lockfile at the given [path] exists and is locked properly.
 * Note: This method cannot be re-entered recursively
 * Note: The same process can only take a given lock once
 */
internal inline fun <T> withFileLock(path: Path, action: () -> T): T {

    /**
     * Intra Process Locking:
     * For the attempt of locking the given [path], we create a new mutex.
     * We then try to promote this mutex into the atomic [fileLockMutex].
     * If another thread currently owns the [path] and has its lock promoted, then
     * we'll wait for this thread to finish the operation (on the mutex).
     *
     * Once we have placed our own lock, we know that all other callers to the [withFileLock] method
     * will wait for us to release the lock.
     *
     * Calling recursively is supported.
     */
    val myLock = ReentrantLock()
    myLock.withLock {
        while (true) {
            val locks = fileLockMutex.get()
            val currentLock = locks[path]

            if (currentLock != null) {
                /*
                 Recursion detected, we are free to return immediately, as this means that we have
                 already acquired the actual file lock.
                 */
                if (currentLock.isHeldByCurrentThread) {
                    return action()
                }

                /**
                 * We try to capture the lock on the current path.
                 * Once we were able to enter, we will attempt to exchange the lock with out own lock
                 */
                if (currentLock.withLock {
                        // Attempt to place my lock.
                        fileLockMutex.compareAndSet(locks, locks + (path to myLock))
                    }) break

                continue
            }

            // Place my lock! Everyone has to capture my lock now.
            if (fileLockMutex.compareAndSet(locks, locks + (path to myLock))) {
                break
            }
        }

        path.createParentDirectories()
        var attempts = 0

        try {
            while (true) {
                try {
                    return FileChannel.open(path, READ, WRITE, CREATE).use { channel ->
                        val lock = channel.lock()
                        lock.use {
                            action()
                        }
                    }
                } catch (_: OverlappingFileLockException) {
                    /**
                     * Overlapping file lock exception can happen if our current process is trying to capture
                     * the file lock while another thread already captured this lock.
                     *
                     *
                     */
                    if (attempts % 128 == 0) {
                        Logger.debug { "Trying to aquire lock '$path'; Waiting for another thread to release the lock..." }
                    }

                    Thread.sleep(64)
                    attempts++
                }
            }
        } finally {
            /**
             * Finally, remove the lock, as we have finished our operation.
             */
            fileLockMutex.updateAndGet { locks -> locks.minus(path) }
        }
    }
}

internal inline fun <T> withDirectoryLock(directory: Path, action: () -> T): T =
    withFileLock(directory.resolve(".lock")) { action() }
