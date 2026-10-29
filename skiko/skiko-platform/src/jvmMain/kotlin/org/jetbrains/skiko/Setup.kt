package org.jetbrains.skiko

expect fun setSystemLookAndFeel()

internal object Setup {
    fun init(
        noEraseBackground: Boolean = System.getProperty("skiko.rendering.noerasebackground") != "false",
        globalLAF: Boolean = System.getProperty("skiko.rendering.laf.global") == "true",
        useScreenMenuBar: Boolean = System.getProperty("skiko.rendering.useScreenMenuBar") != "false",
        autoLinuxDpi: Boolean = System.getProperty("skiko.linux.autodpi") == "true",
        automateGC: Boolean = System.getProperty("skiko.gc.auto") != "false"
    ) {
        if (hostOs == OS.Linux && autoLinuxDpi) {
            val scale = linuxGetSystemDpiScale()
            System.setProperty("sun.java2d.uiScale.enabled", "true")
            System.setProperty("sun.java2d.uiScale", "$scale")
        }
        if (noEraseBackground) {
            // we have to set this property to avoid render flickering.
            System.setProperty("sun.awt.noerasebackground", "true")
        }
        try {
            if (globalLAF) {
                // Setup menu look and feel.
                setSystemLookAndFeel()
            }
            if (useScreenMenuBar) {
                System.setProperty("apple.laf.useScreenMenuBar", "true")
            }
        } catch (e: UnsupportedOperationException) {
            // Not all platforms allow this.
        }

        if (automateGC) {
            FrameWatcher.start()
        }
    }
}

private external fun linuxGetSystemDpiScale(): Float
