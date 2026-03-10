package org.jetbrains.skiko.context

import org.jetbrains.skiko.SkiaLayer

internal abstract class ContextFreeContextHandler(layer: SkiaLayer) : JvmContextHandler(layer) {
    private var isInitialized = false

    override fun initContext(): Boolean {
        if (!isInitialized) {
            isInitialized = true
            onContextInitialized()
        }
        return isInitialized
    }
}