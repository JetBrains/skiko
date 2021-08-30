package org.jetbrains.skija.shaper

import org.jetbrains.skija.*

interface RunHandler {
    /**
     * Called when beginning a line.
     */
    fun beginLine()

    /**
     * Called once for each run in a line. Can compute baselines and offsets.
     */
    fun runInfo(info: RunInfo?)

    /**
     * Called after all [.runInfo] calls for a line.
     */
    fun commitRunInfo()

    /**
     * Called for each run in a line after [.commitRunInfo].
     *
     * @return  an offset to add to every position
     */
    fun runOffset(info: RunInfo?): Point?

    /**
     *
     * Called for each run in a line after [.runOffset].
     *
     *
     * WARN positions are reported from the start of the line, not run, only in Shaper.makeCoreText https://bugs.chromium.org/p/skia/issues/detail?id=10898
     *
     * @param positions  put glyphs[i] at positions[i]
     * @param clusters   clusters[i] is an utf-16 offset starting run which produced glyphs[i]
     */
    fun commitRun(info: RunInfo?, glyphs: ShortArray?, positions: Array<Point?>?, clusters: IntArray?)

    /**
     * Called when ending a line.
     */
    fun commitLine()
}