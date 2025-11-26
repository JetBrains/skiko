package org.jetbrains.skiko

import org.jetbrains.skiko.SkikoProperties.dataPath
import java.nio.channels.FileChannel
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Path
import java.nio.file.StandardOpenOption.*
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.name
import kotlin.use


/**
 * We do use pre-defined lock files to synchronize file system modifications across processes.
 * Note: The lock files shall be used for different purposes
 * Note2: We're relying on String literals and String interning to share a monitor for the entire VM,
 * this shall allow proper locking, even when skiko is loaded in entire isolation, in different ClassLoaders
 */
internal class LockFile private constructor(private val lockfile: Path, private val monitor: Any) {
    val name: String = lockfile.name

    inline fun <T> withLock(action: () -> T): T = withLockFile(monitor, lockfile, action)

    companion object {
        /**
         * Lock file used to synchronize modifications to the 'skiko' native library
         */
        val skiko = LockFile(Path(dataPath, ".skiko.lock"), ".skiko.lock.monitor".intern())

        /**
         * Lock file used to synchronize modifications to the 'angle' native library
         */
        val angle = LockFile(Path(dataPath, ".angle.lock"), ".angle.lock.monitor".intern())
    }
}


/**
 * Runs the given [action] by capturing a file lock on the given [lockfile].
 * Note: The provided [monitor] should is used to synchronize the lock within the current process.
 * It is advisable to use a globally available object.
 */
private inline fun <T> withLockFile(monitor: Any, lockfile: Path, action: () -> T): T {
    /**
     * If we acquired the mutex, then we can be sure that we also have acquired the lockfile, which won't
     * allow to re-enter, therefore, we can just return early
     */
    if (Thread.holdsLock(monitor)) {
        return action()
    }

    synchronized(monitor) {
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
                 * the file lock while another thread already captured this lock,
                 * note: this will only fail if the `dataPathMutex` of another thread was either not
                 * interned correctly, or if another thread captured the lock without using the mutex
                 * (maybe outside of this function?)
                 */
                if (attempts % 128 == 0) {
                    Logger.debug { "Trying to acquire lock '$lockfile'; Waiting for another thread to release the lock..." }
                }

                Thread.sleep(64)
                attempts++
            }
        }
    }
}
