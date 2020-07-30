package org.jetbrains.awthrl.Components

interface Drawable {
    fun redrawLayer()
    fun updateLayer()
    fun disposeLayer()
    val contentScale: Float
}