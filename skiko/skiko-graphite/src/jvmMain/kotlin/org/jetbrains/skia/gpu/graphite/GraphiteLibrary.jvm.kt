package org.jetbrains.skia.gpu.graphite

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.LibraryLoader
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostId
import org.jetbrains.skiko.hostOs

private val graphiteLoader = LibraryLoader("skiko-graphite-$hostId")

private external fun loadVulkanLibrary()

private var isVulkanLibraryLoaded = false

internal actual object GraphiteLibrary {
    @Synchronized
    actual fun load() {
        Library.load()
        graphiteLoader.loadOnce()
        if ((hostOs == OS.Windows || hostOs == OS.Linux) && !isVulkanLibraryLoaded) {
            loadVulkanLibrary()
            isVulkanLibraryLoaded = true
        }
    }
}
