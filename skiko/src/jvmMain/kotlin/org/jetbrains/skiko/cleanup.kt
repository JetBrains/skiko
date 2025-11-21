@file:OptIn(ExperimentalCoroutinesApi::class)

package org.jetbrains.skiko

import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.io.path.*

/**
 * Will asynchronously cleanup 'stale' entries in the [SkikoProperties.dataPath]:
 * If directories are older than [SkikoProperties.dataCleanupDays], then they will be deleted.
 *
 * Cleanup Rule:
 * Everything directory in the [SkikoProperties.dataPath] is expected to maintain a 'last access time'
 * (see [BasicFileAttributeView.readAttributes]). If a given directory was not accessed (is older than) the
 * [SkikoProperties.dataCleanupDays], then it will be deleted.
 *
 * The cleaning can be entirely disabled by setting [SkikoProperties.dataCleanupDays] to `-1`.
 */
internal fun launchSkikoDataDirCleanupIfNecessary() {
    if (SkikoProperties.dataCleanupDays < 0) return

    thread(name = ".skiko cleanup", priority = Thread.MIN_PRIORITY) {
        try {
            doCleanupSkikoDataDir()
        } catch (t: Throwable) {
            Logger.error(t) { "Exception occurred during .skiko cleanup" }
        }
    }
}

private fun doCleanupSkikoDataDir() {
    val dir = Path(SkikoProperties.dataPath)

    dir.listDirectoryEntries().forEach { entry ->
        if (!entry.isDirectory()) return@forEach
        val duration = entry.timeSinceLastAccessed()
        if (duration.toDays() > SkikoProperties.dataCleanupDays) {
            withSikoDataDirectoryLock {
                if (!entry.exists()) return@withSikoDataDirectoryLock
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
