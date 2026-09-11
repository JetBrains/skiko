package org.jetbrains.skiko.benchmarks

import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use

/**
 * Provides short-lived surfaces for benchmark operations.
 *
 * Implementations own the backend resources needed to create each surface. The surface passed to
 * `block` is valid only during the call and is closed before [withSurface] returns.
 */
internal interface BenchmarkSurfaceProvider {
    fun withSurface(width: Int, height: Int, block: (Surface) -> Long): Long

    fun close() = Unit
}

/**
 * CPU-backed benchmark surface provider used as the baseline for GPU comparisons.
 */
internal object RasterBenchmarkSurfaceProvider : BenchmarkSurfaceProvider {
    override fun withSurface(width: Int, height: Int, block: (Surface) -> Long): Long =
        Surface.makeRasterN32Premul(width, height).use(block)
}

/**
 * Lazily creates and owns the GPU-backed surface provider for benchmark cases.
 */
internal object GpuBenchmarkSurfaceProvider {
    private var provider: BenchmarkSurfaceProvider? = null
    private var initialized = false

    fun get(): BenchmarkSurfaceProvider? {
        if (!initialized) {
            provider = makeGpuBenchmarkSurfaceProvider()
            initialized = true
        }

        return provider
    }

    fun isSupported(): Boolean =
        get() != null

    fun close() {
        provider?.close()
        provider = null
        initialized = false
    }
}

/**
 * Creates the best available GPU-backed provider for the current benchmark target.
 *
 * Returns null when the target has no GPU implementation or when the backend context cannot be
 * created in the current environment.
 */
internal expect fun makeGpuBenchmarkSurfaceProvider(): BenchmarkSurfaceProvider?
