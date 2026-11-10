package org.jetbrains.skiko

/**
 * Runtime configuration of experimental Skiko features.
 */
object SkikoFeatureFlags {
    /**
     * Routes text segmentation (graphemes, words and line break opportunities) through the
     * browser-native `Intl.Segmenter` instead of the ICU data embedded into the Skiko binary.
     *
     * Has an effect on web targets (`js` and `wasmJs`) only, is ignored elsewhere.
     */
    var useBrowserSegmenter: Boolean = false
}
