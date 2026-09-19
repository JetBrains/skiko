package org.jetbrains.skiko

import kotlin.time.TimeSource

private val initialTime = TimeSource.Monotonic.markNow()

/** Monotonic frame timestamp, in nanoseconds from one process-wide origin. */
internal fun renderTime() = initialTime.elapsedNow().inWholeNanoseconds
