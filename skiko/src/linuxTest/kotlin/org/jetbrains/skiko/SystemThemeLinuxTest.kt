package org.jetbrains.skiko

import kotlin.test.Test

class SystemThemeLinuxTest {
    @Test
    fun currentSystemThemeDoesNotCrash() {
        when (currentSystemTheme) {
            SystemTheme.DARK, SystemTheme.LIGHT, SystemTheme.UNKNOWN -> Unit
        }
    }
}
