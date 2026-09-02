package org.jetbrains.skiko.swing

/**
 * Standalone entry point for [SkikoFramePacingJitterProbe], to run the probe outside the
 * Gradle test worker (whose environment measurably degrades wait precision on Windows).
 */
object SkikoProbeMain {
    @JvmStatic
    fun main(args: Array<String>) {
        SkikoFramePacingJitterProbe().`measure tick interval statistics`()
    }
}
