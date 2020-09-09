package org.jetbrains.skiko

interface Drawable {
    fun redrawLayer()
    fun updateLayer()
    fun disposeLayer()
    val windowHandle: Long
    val contentScale: Float
}