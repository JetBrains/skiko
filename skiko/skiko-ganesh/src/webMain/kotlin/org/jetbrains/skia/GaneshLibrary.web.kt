package org.jetbrains.skia

internal actual object GaneshLibrary {
    actual fun load() {
        check(isSideModuleLoaded()) {
            "Ganesh side module was not loaded"
        }
    }
}