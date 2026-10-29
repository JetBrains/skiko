package org.jetbrains.skia

import org.jetbrains.skiko.InternalSkikoApi

@InternalSkikoApi
expect object GaneshLibrary {
    fun load()
}