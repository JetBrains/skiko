package org.jetbrains.skiko

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.time.Instant
import kotlin.io.path.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class LibraryLoadCleanupTest {

    /**
     * Will check if launching skiko, using a .skiko data dir to extract the native binary to,
     * will properly cleanup 'old' directories within the data dir.
     * A temp directory will be created, we create two directories: old and new:
     * The old one will be marked as '11' days old. The new one will be 9 days old.
     * We start an isolate, pointing to this directory and setting the cleanup setting to 10 days.
     * The test will assert that the 11 day old directory got deleted, whereas the 9 day old dir is still present.
     */
    @OptIn(ExperimentalPathApi::class)
    @Test
    fun `load library - purges old content in skiko data dir`() = runTest {
        val tempDataDir = createTempDirectory()
        currentCoroutineContext().job.invokeOnCompletion { tempDataDir.deleteRecursively() }

        /*
        Prepare a data dir to contain old and recent directories (note the '{Library.name}-' prefix)
         */
        val oldDirectory = tempDataDir.resolve("${Library.name}-old").createDirectories()
        val newDirectory = tempDataDir.resolve("${Library.name}-skiko-new").createDirectories()

        val oldDirectoryTime = FileTime.from(Instant.now() - Duration.ofDays(11))
        val newDirectoryTime = FileTime.from(Instant.now() - Duration.ofDays(9))
        oldDirectory.updateLastAccessTime(oldDirectoryTime)
        newDirectory.updateLastAccessTime(newDirectoryTime)

        assertTrue(oldDirectory.isDirectory())
        assertTrue(newDirectory.isDirectory())

        withContext(Dispatchers.IO) {
            val process = ProcessBuilder(
                ProcessHandle.current().info().command().get(),
                "-cp", System.getProperty("java.class.path"),
                "-Xmx64m",
                "-Dskiko.data.path=${tempDataDir.absolutePathString()}",
                "-Dskiko.data.cleanup.days=10", Isolate::class.java.name,
            ).start()

            launch {
                process.inputStream.bufferedReader().forEachLine { line ->
                    println(line)
                }
            }

            launch {
                process.errorStream.bufferedReader().forEachLine { line ->
                    System.err.println(line)
                }
            }

            withTimeout(15.seconds) {
                process.onExit().await()
                assertEquals(0, process.exitValue())
            }
        }

        assertFalse("Expected 32 day old directory to be purged", oldDirectory.exists())
        assertTrue("Expected 30 day 'new' directory to still exist", newDirectory.exists())
        assertEquals(newDirectoryTime, newDirectory.getLastAccessTime())

        /*
        Additionally: Test our assertion that the .skiko data dir only contains
        the .lock file and directories.
         */
        tempDataDir.listDirectoryEntries().forEach { file ->
            when {
                file.isReadable() && file.name == LockFile.skiko.name -> return@forEach
                file.isReadable() && file.name == LockFile.angle.name -> return@forEach
                file.isDirectory() -> return@forEach
                else -> error("The cleanup implementation only expects directories and a .lock file\nfound: $file")
            }
        }
    }

    object Isolate {
        @JvmStatic
        fun main(args: Array<String>) {
            Library.load()
        }
    }
}
