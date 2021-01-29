package org.jetbrains.skiko.redrawer

interface Redrawer {
    fun dispose()
    fun needRedraw()
    fun redrawImmediately()
    fun syncSize() = Unit
}