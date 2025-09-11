package org.jetbrains.skiko.context

import org.jetbrains.skia.DirectContext
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.SkiaLayer

internal abstract class ContextBasedContextHandler(layer: SkiaLayer, val name: String) : JvmContextHandler(layer) {

    abstract protected fun makeContext(): DirectContext

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = makeContext()
                onContextInitialized()
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to create Skia $name context!" }
            return false
        }
        return true
    }
}