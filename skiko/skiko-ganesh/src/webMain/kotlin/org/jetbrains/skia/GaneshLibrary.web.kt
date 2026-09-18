package org.jetbrains.skia

import org.jetbrains.skiko.isSideModuleLoaded

internal actual object GaneshLibrary {
    actual fun load() {
        check(isSideModuleLoaded()) {
            "Ganesh side module was not loaded"
        }
    }
}