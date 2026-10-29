package org.jetbrains.skia

import org.jetbrains.skiko.InternalSkikoApi

@InternalSkikoApi
actual object GaneshLibrary {
    actual fun load() {
        check(isSideModuleLoaded()) {
            "Ganesh side module was not loaded"
        }
    }
}
