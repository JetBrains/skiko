package org.jetbrains.skiko

import java.nio.channels.FileChannel
import java.nio.channels.OverlappingFileLockException
import java.nio.file.StandardOpenOption.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.use

val SkikoProperties.dataPathLockFile get() = Path(dataPath, ".lock")

/**
 * We still need a regular intra-process lock to ensure that the process is not trying
 * to capture the file lock more than once.
 */
private val dataPathMutex = ReentrantLock()

/**
 * Locks the '.skiko' directory using a '.lock' file, ensuring synchronization even across
 * process boundaries.
 */
internal inline fun <T> withSikoDataDirectoryLock(action: () -> T): T {
    /**
     * If we're holding the mutex, then we can be sure that we also have aquired the lockfile, which won't
     * allow to re-enter, therefore, we can just return eaerly
     */
    if (dataPathMutex.isHeldByCurrentThread) {
        return action()
    }

    dataPathMutex.withLock {
        val lockfile = SkikoProperties.dataPathLockFile

        lockfile.createParentDirectories()
        var attempts = 0

        while (true) {
            try {
                return FileChannel.open(lockfile, READ, WRITE, CREATE).use { channel ->
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
                    Logger.debug { "Trying to aquire lock '$lockfile'; Waiting for another thread to release the lock..." }
                }

                Thread.sleep(64)
                attempts++

            }
        }
    }
}
