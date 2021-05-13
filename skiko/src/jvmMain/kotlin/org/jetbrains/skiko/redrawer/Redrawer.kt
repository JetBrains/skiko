package org.jetbrains.skiko.redrawer

interface Redrawer {
    fun dispose()
    fun needRedraw()
    fun redrawImmediately()
    suspend fun awaitRedraw(): Boolean
    fun syncSize() = Unit
}