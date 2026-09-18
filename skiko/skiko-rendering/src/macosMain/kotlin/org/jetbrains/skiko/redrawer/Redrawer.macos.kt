package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.renderTime

/**
 * What [org.jetbrains.skiko.SkiaLayer] drives on native macOS, implemented once per graphics API.
 */
internal interface Redrawer {
    fun dispose()
    fun needRender(throttledToVsync: Boolean)
    fun renderImmediately()
    fun syncBoundsFromPlatformComponent()
    fun update(nanoTime: Long = renderTime())
    val renderInfo: String
}
