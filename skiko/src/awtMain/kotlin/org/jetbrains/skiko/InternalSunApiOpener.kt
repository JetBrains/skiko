package org.jetbrains.skiko

import net.bytebuddy.agent.ByteBuddyAgent
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

    private fun canAccessSunFontApi() =
        try {
            val fontManagerClass = Class.forName("sun.font.FontManagerFactory")
            fontManagerClass.getDeclaredMethod("getInstance").invoke(null)
            val font2DClass = Class.forName("sun.font.Font2D")
            fontManagerClass.getDeclaredMethod("getInstance").invoke(null)
            true
        } catch (e: IllegalAccessException) {
            false
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
