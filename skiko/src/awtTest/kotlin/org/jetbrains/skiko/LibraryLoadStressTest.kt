@file:OptIn(ExperimentalCoroutinesApi::class)

package org.jetbrains.skiko


import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.test.fail
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Special test which tests if loading the native library works 'under stress',
 * where multiple processes try to launch at once, trying to load (and potentially unpack) skiko.
 * The test therefore spawns multiple processes in parallel:
 * Process A is called 'loader' which just tries to load (potentially unpack) the library
 * Process B is called 'deleter' which deletes the .skiko folder
 *
 * Since multiple processes A are launched in parallel, at any given time,
 * the 'deleter' provokes the 'loader' processes to race with each other.
 * If not properly synchronized, those loaders will fail because of corrupted .skiko directories
 *
 */
@ExperimentalPathApi
class LibraryLoadStressTest {

    /**
     * Testing if library loading works if the 'skiko data dir' is not yet present on disk
     */
    @Test
    fun `load library in empty directory`() = runTest {
        val tempDataDir = Files.createTempDirectory("skiko-tests")
        val nonExistingDir = tempDataDir.resolve("non-existing-dir")
        coroutineContext.job.invokeOnCompletion { tempDataDir.deleteRecursively() }
        launchProcess("load", nonExistingDir)
    }

    @Test
    fun `load library - stress test`() = runTest(timeout = 10.minutes) {
        val tempDataDir = Files.createTempDirectory("skiko-stress-test")
        coroutineContext.job.invokeOnCompletion { tempDataDir.deleteRecursively() }

        val parallelism = 4
        val repetitions = 32
        repeat(repetitions) { repetitionIdx ->
            Logger.info { "running test repetition #$repetitionIdx" }

            coroutineScope {
                repeat(parallelism) { loaderIdx ->
                    launch(Dispatchers.IO + CoroutineName("loader: $loaderIdx, repetition: $repetitionIdx")) {
                        launchProcess("load", tempDataDir)
                    }
                }
            }

            withContext(Dispatchers.IO) {
                Logger.info { "Cleanup" }
                launchProcess("delete", tempDataDir)
            }
        }
    }

    suspend fun launchProcess(
        command: String, skikoDataDir: Path
    ) = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            ProcessHandle.current().info().command().get(),
            "-cp", System.getProperty("java.class.path"),
            "-Dskiko.data.path=${skikoDataDir.toAbsolutePath()}",
            "-Xmx256m", "-Xms64m",
            StressTestMain::class.java.name, command
        ).start()

        coroutineContext.job.invokeOnCompletion {
            process.destroyForcibly()
        }

        launch(Dispatchers.IO) {
            process.inputStream.bufferedReader().forEachLine { line ->
                println("$command: $line")
            }
        }

        val errorOut = async(Dispatchers.IO) {
            process.errorStream.bufferedReader().readText()
        }

        select {
            process.onExit().asDeferred().onAwait { }
            onTimeout(15.seconds) {
                process.destroyForcibly()
                fail("Timeout waiting on '$command' to finish")
            }
        }

        if (process.exitValue() != 0) {
            fail("Process ($command) exited with code ${process.exitValue()}: ${errorOut.await()}")
        }
    }

    /**
     * This class is the target for spawning new processes:
     * It can act as 'loader' or 'deleter' depending on the argument ("load" or "delete")
     */
    object StressTestMain {
        @JvmStatic
        fun main(args: Array<String>) {
            when (val operation = args.first()) {
                "load" -> load()
                "delete" -> delete()
                else -> error("Operation not supported: '$operation'")
            }
        }

        private fun load() {
            Library.load()
            /*
            Check if we can access the 'currentSystemTheme' as this will perform an actual native call
             */
            currentSystemTheme
        }

        @OptIn(ExperimentalPathApi::class)
        private fun delete() {
            val dataDir = Path(SkikoProperties.dataPath)
            if (dataDir.exists()) {
                dataDir.listDirectoryEntries().forEach { entry ->
                    entry.deleteRecursively()
                }
            }
        }
    }
}
