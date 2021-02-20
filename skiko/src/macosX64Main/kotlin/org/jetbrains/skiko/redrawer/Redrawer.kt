package org.jetbrains.skiko.native.redrawer

// TODO: this is exact copy of jvm counterpart. Commonize!
interface Redrawer {
    fun dispose()
    fun needRedraw()
    fun redrawImmediately()
    fun syncSize() = Unit
}