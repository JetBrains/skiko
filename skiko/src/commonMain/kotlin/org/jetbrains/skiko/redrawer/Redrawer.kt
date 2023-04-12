package org.jetbrains.skiko.redrawer

internal interface Redrawer {
    fun dispose()
    fun needRedraw()
    fun redrawImmediately()
    fun syncSize() = Unit
    fun setVisible(isVisible: Boolean) = Unit
    val renderInfo: String
}