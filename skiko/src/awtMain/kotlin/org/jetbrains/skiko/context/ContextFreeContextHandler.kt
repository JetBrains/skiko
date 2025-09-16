package org.jetbrains.skiko.context

import org.jetbrains.skiko.SkiaLayer

internal abstract class ContextFreeContextHandler(layer: SkiaLayer) : JvmContextHandler(layer) {
    private var isInited = false

    override fun initContext(): Boolean {
        if (!isInited) {
            isInited = true
            onContextInitialized()
        }
        return isInited
    }
}