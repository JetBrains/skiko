package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.context.ContextHandler

internal abstract class Redrawer(
    protected val contextHandler: ContextHandler
) {
    open fun dispose() = contextHandler.dispose()
    open fun needRedraw() = Unit
    open fun redrawImmediately() = Unit
    open fun syncSize() = Unit

    val renderInfo: String get() = contextHandler.rendererInfo()
}