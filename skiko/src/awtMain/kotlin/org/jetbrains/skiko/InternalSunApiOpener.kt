package org.jetbrains.skiko

import net.bytebuddy.agent.ByteBuddyAgent
import java.awt.Font
import java.lang.instrument.Instrumentation
import java.util.concurrent.atomic.AtomicBoolean

internal object InternalSunApiOpener {

    private var isSunFontAccessible = AtomicBoolean(false)

    fun ensureAccessToSunFontPackage() {
        if (isSunFontAccessible.get()) return

        if (canAccessSunFontApi()) {
            isSunFontAccessible.set(true)
            return
        }

        try {
            val instrumentation = ByteBuddyAgent.install()
            instrumentation.addOpenDirective("sun.font")
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        isSunFontAccessible.set(true)
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

            // Try to obtain the proper font family from an AWT Font (will fail if not running on JBR)
            with(AwtFontUtils) {
                Font(Font.DIALOG, 10, Font.PLAIN).fontFamilyName
            }

            return true
        } catch (e: Throwable) {
            return false
        }
    }

    private fun Instrumentation.addOpenDirective(packageName: String) {
        val unnamedModule = setOf(ClassLoader.getSystemClassLoader().unnamedModule)
        val module = ModuleLayer.boot().modules().single { it.name == "java.desktop" }
        redefineModule(
            /* module = */ module,
            /* extraReads = */ unnamedModule,
            /* extraExports = */ mapOf(packageName to unnamedModule),
            /* extraOpens = */ mapOf(packageName to unnamedModule),
            /* extraUses = */ emptySet(),
            /* extraProvides = */ emptyMap()
        )

        if (canAccessSunFontApi()) {
            println("Opened access to sun.font package.")
        } else {
            println("Opening access to sun.font package failed.")
        }
    }
}
