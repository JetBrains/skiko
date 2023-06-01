package org.jetbrains.skiko.util

import org.jetbrains.skiko.InternalSunApiChecker
import kotlin.test.assertTrue

internal fun assertOpensAreSet() {
    assertTrue(InternalSunApiChecker.isSunFontApiAccessible(), "The java.desktop/sun.font module doesn't seem to be opened")
}
