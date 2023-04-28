package org.jetbrains.skiko.redrawer

internal interface Redrawer {
    fun dispose()
    fun needRedraw()
    fun redrawImmediately()
    fun syncSize() = Unit
    fun setVisible(isVisible: Boolean) = Unit
    // ARGB encoded as in org.jetbrains.skia.Color
    fun setLayerBackground(color: Int) = Unit
    val renderInfo: String
}