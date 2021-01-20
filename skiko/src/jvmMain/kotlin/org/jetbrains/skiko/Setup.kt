package org.jetbrains.skiko

import javax.swing.UIManager

// object Setup {
//     fun init(globalLAF: Boolean = false, noEraseBackground: Boolean = true) {
//         if (noEraseBackground) {
//             // we have to set this property to avoid render flickering.
//             System.setProperty("sun.awt.noerasebackground", "true")
//         }
//         try {
//             if (globalLAF) {
//                 // Setup menu look and feel.
//                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
//                 System.setProperty("apple.laf.useScreenMenuBar", "true")
//             }
//         } catch (e: UnsupportedOperationException) {
//             // Not all platforms allow this.
//         }
//     }
// }

object Setup {
    fun init(globalLAF: Boolean = false, noEraseBackground: Boolean = true) {
        if (noEraseBackground) {
            // we have to set this property to avoid render flickering.
            System.setProperty("sun.awt.noerasebackground", "true")
        }
        when(hostOs) {
            OS.MacOS -> {
                System.setProperty("apple.laf.useScreenMenuBar", "true")
            }
            else -> {
                if (globalLAF) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                }
            }
        }
    }
}