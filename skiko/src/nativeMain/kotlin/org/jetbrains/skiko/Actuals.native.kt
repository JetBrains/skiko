package org.jetbrains.skiko

import kotlin.time.TimeSource

private val markNow = TimeSource.Monotonic.markNow()
actual fun currentNanoTime(): Long = markNow.elapsedNow().inWholeNanoseconds