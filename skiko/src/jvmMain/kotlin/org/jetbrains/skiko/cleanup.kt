@file:OptIn(ExperimentalCoroutinesApi::class)

package org.jetbrains.skiko

import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.io.path.*

/**
 * Will asynchronously cleanup 'stale' entries in the [SkikoProperties.dataPath]:
 * If directories are older than [SkikoProperties.dataCleanupDays], then they will be deleted.
 */
internal fun launchSkikoDataDirCleanupIfNecessary(dataDir: Path) {
    thread(
        name = ".skiko cleanup",
        priority = Thread.MIN_PRIORITY
    ) {
        try {
            withDirectoryLock(dataDir) {
                doCleanupSkikoDataDir()
            }
        } catch (t: Throwable) {
            Logger.error(t) { "Exception occurred during .skiko cleanup" }
        }
    }
}

private fun doCleanupSkikoDataDir() {
    val dir = Path(SkikoProperties.dataPath)

    dir.listDirectoryEntries().forEach { entry ->
        if (!entry.isDirectory()) return@forEach
        val duration = entry.timeSinceLastModified()
        if (duration.toDays() > SkikoProperties.dataCleanupDays) {
            Logger.debug { "Cleaning up '${entry.name}' in '.${dir.name}' directory after ${duration.toDays()}" }
            entry.toFile().deleteRecursively()
        }
    }
}

private fun Path.timeSinceLastModified(): Duration {
    val lastModifiedTime = getLastModifiedTime().toInstant()
    return Duration.between(lastModifiedTime, Instant.now())
}
