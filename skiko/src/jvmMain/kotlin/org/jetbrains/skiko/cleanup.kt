@file:OptIn(ExperimentalCoroutinesApi::class)

package org.jetbrains.skiko

import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.time.Instant
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.io.path.*


/**
 * Cleanup actions will be enqueued on this executor if needed.
 * Note: We're not using [java.util.concurrent.Executors.newSingleThreadExecutor] here,
 * as this executor will keep the provided thread (and therefore JVM process) until it is shut down
 * Using a daemon thread is however not desirable for the cleanup as well, as it will just be stopped
 * during shutdown, leaving us with corrupted states.
 *
 * Therefore, a threadpool executor which only spawns a single thraed when needed and closes this thread
 * once all scheduled work is done seems like the correct choice.
 */
private val cleanupExecutor = ThreadPoolExecutor(
    /* corePoolSize = */ 0, /* maximumPoolSize = */1,
    /* keepAliveTime = */0, /* unit = */TimeUnit.SECONDS,
    /* workQueue = */LinkedBlockingQueue()
) { runnable ->
    thread(start = false, isDaemon = false, name = "skiko-cleanup-thread") { runnable.run() }
}

/**
 * Will asynchronously clean up 'stale' entries in the [SkikoProperties.dataPath]:
 * If directories are older than [SkikoProperties.dataCleanupDays], then they will be deleted.
 *
 * Cleanup Rule:
 * Everything directory in the [SkikoProperties.dataPath] is expected to maintain a 'last access time'
 * (see [BasicFileAttributeView.readAttributes]). If a given directory was not accessed (is older than) the
 * [SkikoProperties.dataCleanupDays], then it will be deleted.
 *
 * The cleaning can be entirely disabled by setting [SkikoProperties.dataCleanupDays] to `-1`.
 */
internal fun enqueueSkikoDataDirCleanupIfNecessary(lockFile: LockFile, glob: String) {
    if (SkikoProperties.dataCleanupDays < 0) return
    cleanupExecutor.execute {
        try {
            doCleanupSkikoDataDir(lockFile, glob)
        } catch (t: Throwable) {
            Logger.error(t) { "Exception occurred during .skiko cleanup" }
        }
    }
}

private fun doCleanupSkikoDataDir(lockFile: LockFile, glob: String) {
    val dir = Path(SkikoProperties.dataPath)
    dir.listDirectoryEntries(glob).forEach { entry ->
        if (!entry.isDirectory()) return@forEach

        lockFile.withLock {
            /*
            Catch the case where another process could _eventually_ delete the directory right before us
            trying to read the 'lastAccessTime'
             */
            val duration = try {
                entry.timeSinceLastAccessed()
            } catch (_: FileNotFoundException) {
                return@forEach
            }

            if (duration.toDays() > SkikoProperties.dataCleanupDays) {
                if (!entry.exists()) return@withLock
                Logger.debug { "Cleaning up '${entry.name}' in '.${dir.name}' directory after ${duration.toDays()}" }
                entry.toFile().deleteRecursively()
            }
        }
    }
}

private fun Path.timeSinceLastAccessed(): Duration {
    val lastModifiedTime = Files.readAttributes(this, BasicFileAttributes::class.java).lastAccessTime().toInstant()
    return Duration.between(lastModifiedTime, Instant.now())
}

internal fun Path.updateLastAccessTime(instant: Instant = Instant.now()) {
    updateLastAccessTime(FileTime.from(instant))
}

internal fun Path.updateLastAccessTime(fileTime: FileTime) {
    Files.getFileAttributeView(this, BasicFileAttributeView::class.java)
        .setTimes(null, /* lastAccessTime */ fileTime, null)
}

internal fun Path.getLastAccessTime(): FileTime =
    Files.readAttributes(this, BasicFileAttributes::class.java).lastAccessTime()
