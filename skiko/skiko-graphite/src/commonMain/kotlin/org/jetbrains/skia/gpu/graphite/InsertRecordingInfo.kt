package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skiko.ExperimentalSkikoApi

/**
 * Information passed when inserting a [Recording] into a [GraphiteContext].
 *
 * @param recording recording to insert.
 * @param waitSemaphores semaphores for the GPU to wait on before executing the recording.
 * @param signalSemaphores semaphores for the GPU to signal after executing the recording.
 */
@ExperimentalSkikoApi
class InsertRecordingInfo(
    val recording: Recording,
    val waitSemaphores: Array<BackendSemaphore> = emptyArray(),
    val signalSemaphores: Array<BackendSemaphore> = emptyArray(),
)
