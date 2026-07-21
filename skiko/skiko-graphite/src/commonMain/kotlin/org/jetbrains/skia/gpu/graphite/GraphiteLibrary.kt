package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

internal expect object GraphiteLibrary {
    fun load()
}

internal fun requireMetalSupport() {
    when (hostOs) {
        OS.MacOS, OS.Ios, OS.Tvos -> Unit
        else -> throw UnsupportedOperationException("Graphite Metal is not supported on ${hostOs.id}")
    }
}
