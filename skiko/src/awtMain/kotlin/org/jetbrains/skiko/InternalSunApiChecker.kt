package org.jetbrains.skiko

import java.util.concurrent.atomic.AtomicBoolean

internal object InternalSunApiChecker {

    private var hasCheckedAccess = AtomicBoolean(false)
    private var isSunFontAccessible = AtomicBoolean(false)

    fun isSunFontApiAccessible(): Boolean {
        if (hasCheckedAccess.get()) return isSunFontAccessible.get()

        if (!isRunningOnJetBrainsRuntime()) {
            logJbrWarning()
        }

        val canAccess = canAccessSunFontApi()
        if (!canAccess) {
            logInstructions()
        }
        isSunFontAccessible.set(canAccess)
        hasCheckedAccess.set(true)
        return canAccess
    }

    private fun canAccessSunFontApi(): Boolean {
        try {
            val unnamedModule = ClassLoader.getSystemClassLoader().unnamedModule
            val desktopModule = ModuleLayer.boot().modules().single { it.name == "java.desktop" }

            // Check the necessary open directives are available, so we can access standard sun.font APIs
            if (!unnamedModule.canRead(desktopModule)) return false
            if (!desktopModule.isOpen("sun.font", unnamedModule)) return false

            // Try to obtain an instance of sun.font.FontManager (will fail if the open directive is missing)
            val fontManagerClass = Class.forName("sun.font.FontManagerFactory")
            fontManagerClass.getDeclaredMethod("getInstance").invoke(null)

            Logger.debug { "Sun Font APIs are accessible, advanced font features are available" }
            return true
        } catch (ignored: Throwable) {
            return false
        }
    }

    private fun logJbrWarning() {
        Logger.warn {
            """
                |The Java Runtime in use may not support all advanced Skiko features.
                |It is recommended that you run this app on the JetBrains Runtime for
                |best results.
                """.trimMargin()
        }
    }

    private fun logInstructions() {
        Logger.error {
            """
            |
            |!!! WARNING !!!
            |For Skiko to run optimally, you should add the following argument
            |to the command for this program:
            |
            |--add-opens java.desktop/sun.font=ALL-UNNAMED 
            |
            |This is required to be able to properly match the Skia fonts with
            |the AWT fonts and access private JDK APIs used for some advanced
            |features.
            """.trimMargin()
        }
    }
}
