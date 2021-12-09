package org.jetbrains.skiko

import javax.swing.UIManager

actual fun setSystemLookAndFeel() = UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())