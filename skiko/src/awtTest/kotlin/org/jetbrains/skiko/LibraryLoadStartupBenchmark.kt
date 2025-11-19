@file:OptIn(ExperimentalPathApi::class)

package org.jetbrains.skiko

import kotlinx.benchmark.*
import kotlinx.benchmark.Setup
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString

@Suppress("unused")
@State(Scope.Benchmark)
open class LibraryLoadStartupBenchmark {


    lateinit var skikoDataDir: Array<Path>

    val iteration = AtomicInteger(0)

    @Setup
    fun setup() {
        skikoDataDir = Array(100) { Files.createTempDirectory("skiko-data-$it") }
    }

    @TearDown
    fun tearDown() {
        skikoDataDir.forEach { it.toFile().deleteRecursively() }
    }

    /**
     * 19.11 | Mac M4
     * N = 100
     *   mean =      0.085 ±(99.9%) 0.013 s/op
     *
     * // Before .lock file
     *   N = 100
     *   mean =      0.085 ±(99.9%) 0.011 s/op
     */
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Measurement(100)
    fun warmStartup() {
        startApplicationAndWait(skikoDataDir[0])
    }

    /**
     * 19.11 | Mac M4
     * N = 100
     *   mean =      0.419 ±(99.9%) 0.005 s/op
     *
     * // Before .lock file
     *   N = 100
     *   mean =      0.423 ±(99.9%) 0.009 s/op
     */
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Measurement(100)
    fun coldStartup() {
        startApplicationAndWait(skikoDataDir[iteration.getAndIncrement()])
    }

    private fun startApplicationAndWait(skikoDataDir: Path) {
        val process = ProcessBuilder(
            ProcessHandle.current().info().command().get(),
            "-cp", System.getProperty("java.class.path"),
            "-Xmx64m", "-Xms64m",
            "-Dskiko.data.path=${skikoDataDir.absolutePathString()}",
            Startup::class.java.name
        ).start()

        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            throw AssertionError("Process didn't end in 5 seconds")
        }

        if (process.exitValue() != 0) {
            val failure = process.errorStream.reader().readText()
            val stdout = process.inputStream.reader().readText()
            throw AssertionError("Process failed (${process.exitValue()})" + "\n" + failure + "\n" + stdout)
        }
    }

    @Suppress("unused") // Launched in separate process
    object Startup {
        @JvmStatic
        fun main(args: Array<String>) {
            Library.load()
            currentSystemTheme
        }
    }
}
