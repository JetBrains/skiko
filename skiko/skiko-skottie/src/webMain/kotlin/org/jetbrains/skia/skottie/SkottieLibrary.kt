package org.jetbrains.skia.skottie

internal actual object SkottieLibrary {
    actual fun load() {
        check(isSideModuleLoaded()) {
            "Skottie side module was not loaded"
        }
    }
}
